use strict;
use warnings;
use utf8;

my $filename = 'keys.pot';
open my $fp, '<:encoding(utf8)', $filename or die "Unable to read keys.pot";
open my $out, '>:encoding(utf8)', 'xx_PIRATE.po' or die "Unable to write to pirate.po";

my %dict = (
    "you've" => "ye've",
    'your' => 'yer',
    'you' => 'ye',
    'am' => 'be',
    '\sis' => ' be',
    'are' => 'be',
    'my' => 'me',
    'them' => "'em",
    'no' => 'nay',
    'yes' => 'aye',
    'and' => "an'",
    'player' => 'pirate',
    'players' => 'pirates',
    'member' => 'mates',
    'warning' => 'avast',
    'error' => 'blimey',
    'wrong' => 'awry',
    'leader' => 'captain',
    'saving' => "storin'",
    'reward' => 'bounty',
    'level' => 'treasure',
    'levels' => 'treasure',
);
my %part_dict = (
    'ing' => "in'"
);
sub get_pirate {
    my ($txt) = @_;
    my $pirate = $txt;
    utf8::encode($pirate);
    print "txt: $txt\n";
    foreach my $key (keys %dict) {
        $pirate =~ s/\b((§[0-9a-fklmor])?)$key\b/$1$dict{$key}/g;
        my $ukey = ucfirst($key);
        my $uvalue = ucfirst($dict{$key});
        $pirate =~ s/\b((§[0-9a-fklmor])?)$ukey\b/$1$uvalue/g;
    }
    foreach my $key (keys %part_dict) {
        $pirate =~ s/$key/$part_dict{$key}/g;
    }
    if ($pirate =~ m/\{0/) {
        $pirate =~ s/''/'/g;
        $pirate =~ s/'/''/g;
    }
    utf8::decode($pirate);
    return $pirate;
}

while (my $line = <$fp>) {
    chomp $line;
    if (! ($line =~ m/#: .*$/)) {
        # Skip line comments
        $line =~ s/^\"Language: \\n/\"Language: xx-pirate\\n/g;
        $line =~ s/^\"Last-Translator: FULL NAME <EMAIL\@ADDRESS>\\n/\"Last-Translator: R4zorax\\n/g;
        print $out "$line\n";
    }
    if ($line =~ m/^msgid \"(?<txt>.*)\"$/) {
        my $txt = $+{txt};
        while (($line = <$fp>) =~ m/^\"(?<txt>.*)\"$/) {
          $line = $+{txt};
          $txt .= "\n" . $line;
          print $out "\"$line\"\n";
        }
        my $pirate = get_pirate($txt);
        print "pirate: $pirate\n";
        $pirate =~ s/\n/\"\n\"/g;
        print $out "msgstr \"$pirate\"\n";
    }
}
close $fp;
close $out;
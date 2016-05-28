use warnings;
use strict;
use utf8;
binmode STDOUT, "utf8";
my $filename = 'keys.pot';
open my $fp, '<:utf8', $filename or die "Unable to read keys.pot";
open my $out, '>:encoding(utf8)', 'xx_lol_US.po' or die "Unable to write to lol_US.po";

my %dict = (
    "you've" => "kitteh haz",
    'your' => "kitteh's",
    'am' => 'iz',
    'is' => ' iz',
    'are' => 'be',
    'my' => 'mah',
    'them' => "'dem",
    'no' => 'noe',
    'and' => "an'",
    'has' => 'haz',
    'was' => 'wuz',
    'player' => 'kitteh',
    'players' => 'kittehs',
    'member' => 'cats',
    'warning' => 'oh noes',
    'error' => 'yikes',
    'wrong' => 'awry',
    'leader' => 'top cat',
    'saving' => "storin'",
    'reward' => 'kitteh treet',
    'your' => 'yoor',
    '\sisland' => ' cathouz',
    'requirements' => 'stuffs needed',
    'currency' => 'moniez',
    'Item' => 'stuffs',
    'the' => 'teh',
    'block' => 'blok',
    'blocks' => 'bloks',
    'biomes' => 'biomz',
    'biome' => 'baium',
    'grass' => 'graz',
    'more' => 'moar',
    'requires' => 'rekwirz',
    'days' => 'dais',
    'within' => 'wiffin',
    'protects' => 'proteks',
    'purge' => 'purrrge',
    'orphans' => 'orfanz',
    'invitation' => 'invitashun',
);
my %part_dict = (
    'an cathouz' => 'a cathouz',
    'iz not available' => 'kitteh no can haz',
    'iz not repeatable' => 'can haz onlie wunce',
    'teh following bloks short' => 'no haz dese bloks',
);
sub get_lolcat {
    my ($txt) = @_;
    my $lolcat = $txt;
    utf8::encode($lolcat);
    print "txt: $txt\n";
    foreach my $key (keys %dict) {
        $lolcat =~ s/\b((§[0-9a-fklmor])?)$key\b/$1$dict{$key}/g;
        my $ukey = ucfirst($key);
        my $uvalue = ucfirst($dict{$key});
        $lolcat =~ s/\b((§[0-9a-fklmor])?)$ukey\b/$1$uvalue/g;
    }
    foreach my $key (keys %part_dict) {
        $lolcat =~ s/$key/$part_dict{$key}/g;
    }
    if ($lolcat =~ m/\{0/) {
        $lolcat =~ s/''/'/g;
        $lolcat =~ s/'/''/g;
    }
    utf8::decode($lolcat);
    return $lolcat;
}

while (my $line = <$fp>) {
    chomp $line;
    if (! ($line =~ m/#: .*$/)) {
        # Skip line comments
        $line =~ s/^\"Language: \\n/\"Language: xx_lol_US\\n/g;
        $line =~ s/^\"Last-Translator: FULL NAME <EMAIL\@ADDRESS>\\n/\"Last-Translator: Woolwind\\n/g;
        print $out "$line\n";
    }
    if ($line =~ m/^msgid \"(?<txt>.*)\"$/) {
        my $txt = $+{txt};
        while (($line = <$fp>) =~ m/^\"(?<txt>.*)\"$/) {
          $line = $+{txt};
          $txt .= "\n" . $line;
          print $out "\"$line\"\n";
        }
        my $lolcat = get_lolcat($txt);
        print "lolcat: $lolcat\n";
        $lolcat =~ s/\n/\"\n\"/g;
        print $out "msgstr \"$lolcat\"\n";
    }
}
close $fp;
close $out;

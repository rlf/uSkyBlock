use strict;
use warnings;
use URI::Encode qw(uri_encode);
use utf8;

my $filename = 'keys.pot';
my $arrurl = 'http://isithackday.com/arrpi.php?text=';
open my $fp, '<:encoding(UTF-8)', $filename or die "Unable to read keys.pot";
open my $out, '>:encoding(UTF-8)', 'xx-pirate.po' or die "Unable to write to pirate.po";

while (my $line = <$fp>) {
    chomp $line;
    if (! ($line =~ m/#: .*$/)) {
        # Skip line comments
        print $out "$line\n";
    }
    if ($line =~ m/msgid \"(?<txt>.*)\"$/) {
        my $txt = $+{txt};
        while (($line = <$fp>) =~ m/^\"(?<txt>.*)\"$/) {
          $line = $+{txt};
          $line =~ s/^Language: \\n$/Language: xx-pirate\\n/g;
          $txt .= "\n" . $line;
          print $out "\"$line\"\n";
        }
        $txt = uri_encode($txt);
        $txt =~ s/#/%23/g;
        my $uri = $arrurl . $txt;
        my $pirate = `curl -g --silent "$uri"`;
        die "Unable to get pirate speak from url $uri! to translate $txt: $!" if $? ne 0;
        utf8::decode($pirate);
        $pirate =~ s/\n/\"\n\"/g;
        print $out "msgstr \"$pirate\"\n";
    }
}
close $fp;
close $out;
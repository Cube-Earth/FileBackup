# FileBackup

# Command Line Options

Here is the breakdown of the available options and their descriptions:

| Short Option | Long Option | Argument Required? | Description |
| --- | --- | --- | --- |
| `-h` | `--help` | No | Display the help page. |
| `-v` | `--verbose` | No | Increase verbosity. |
| `-s` | `--src` | Yes | Source directory. |
| `-d` | `--dst` | Yes | Destination directory. |
| `-a` | `--action` | Yes | Action to perform. |
| `-t` | `--timespan` | Yes | Approximate time span to perform action. This will limit the number of files being processed.<br>The last processed directory will be marked and the next run will start at this point.<br>The format of the time span is: `<integer>y<integer>M<integer>d<integer>h<integer>m<integer>s<integer>S`|
| `-r` | `--remark` | Yes | Remark. |
| *None* | `--probe` | Yes | Enable probe mode (not working for all actions). |


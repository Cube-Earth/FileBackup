# FileBackup

## Purpose

The application name is kind of misleading. The main purpose is to find duplicates (on e.g. a NAS server) and to delete them (or at least preventing that duplicates will increase).

Why? --> Usually having different places e.g. for photos over years there are different storage locations (e.g. external hard disks, laptops). On laptops or on the NAS the structure is changing to organize the photos. Plain 3-way diffs won't work anymore.

Features:
- A sqlite database is used to "register" all files (inode ID, inode change time, file path, file type, size, sha256 checksum)
- This application is designed for Linux systems because the concept relies on inodes
- An inode has its own change time. Whenever a file (the inode) is touched, the inode change time will be updated to the current time. This includes even if the file modification time is changed. With this mechanism, changed files (inodes) can be reliable and efficient recognized and only for these changes e.g. the sha256 checksum will be recalculated.
- The sqlite database is used to quickly find duplicates and sha256 checksums.
- Files are never deleted. If they are deduplicated, they will be moved in its own directory. If they are removed (because of the remove actions), they will be moved in an archive directory. Both directories need to be inspected / deleted manually (with that it is your own decision).
- MacOS files can have sidecar files (tags, temporary working copies). These are not registered and handled liked normal files. If their "parent" files are (re-)moved, the same automatically applies to the sidecar files. If the "parent" file is deduplicated the sidecar files remain untouched. 


## Command Line Options

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



## Actions

| Action Name | Purpose | Source/Destination Directory Needed? |
| --- | --- | --- |
| deduplicate_all | Iterates over all files. Duplicates will be replaced by hard links. |  |
| deduplicate_most | Iterates over all files larger than 2MB. Duplicates will be replaced by hard links. |  |
| register_files | Iterates over all files and registers them inside the sqlite database |  |
| ban_files | Registers all files as banned and moves them to a "banned" directory for manual inspection/deletion. This prevents that already deleted files will "resurrect" in case a external/backup hard disk will be "re-copied" / "re-inspected". |  |
| find_cloned_directories | Find for the source directory, other directories with a high matching ratio. This is the base for the "remove*" or "show_duplicates" actions (to drill down). | s |
| remove_these_duplicates | Remove all duplicates from this directory (really removing, so that the remaining files can be manually sorted; removing means moving to an Archive folder) |  |
| remove_other_duplicates | Remove all duplicates from outside this directory (really removing, so that the remaining files can be manually sorted; removing means moving to an Archive folder) | s |
| remove_all_other_duplicates | TODO |  |
| show_duplicates | Show files in this base directory which are duplicates |  |
| copy_files | Copy files from e.g. an external hard disk / sd ram onto the NAS server (duplicates will end up in hard links). New files will be automatically registered. | s, d |
| move_files | Move files from e.g. an external hard disk / sd ram onto the NAS server (duplicates will end up in hard links). New files will be automatically registered. | s, d |
| clean_database | Purges obsolete database records. |  |

If neither the source nor the destination directory is required, the current directory will be used as base directory.
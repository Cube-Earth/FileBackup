Commands

install sqlite3 cli on QNAP:
opkg install sqlite3-cli
sqllite3
> .help
> .open VolumeFiles.db
> select * from file limit 20;
> .quit


nohup java -jar /share/MD0_DATA/Data/\@admin/FileBackup/FileBackup.jar -s /share/MD0_DATA/Data -a register_files &

java -jar /share/MD0_DATA/Data/\@admin/FileBackup/FileBackup.jar -s "$PWD" -a fcd

#!/bin/sh

function findHousekeeperDir
{
  if [ "$1" == "/" ]
  then
    echo "   ERROR: no housekeeper directory found. skipping ..."
    return 1
  fi

  if [ -d "$1/.housekeeper" ]
  then
    baseDir="$1"
    housekeeperDir="$1/.housekeeper"
  else
    findHousekeeperDir "$(dirname -- "$1")"
  fi
}

for f in "$@"
do
  f="$(cd "$(dirname -- "$1")" >/dev/null; pwd -P)/$(basename -- "$1")"
  echo "deleting file $f ..."
  if findHousekeeperDir "$(dirname -- "$f")"
  then
    relPath="${f##$baseDir/}"
    t="$housekeeperDir/VolumeFiles/deletions/$relPath"
    echo "   moving to $t ..."
#    mkdir -p "$(dirname -- "$t")"
#    mv "$f" "$t"
  fi
done

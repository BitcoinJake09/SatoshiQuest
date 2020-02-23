find . -type f -name "*" -exec sed -i'' -e 's/satoshiquest/changeme/g' {} +
find . -type f -name "*" -exec sed -i'' -e 's/SatoshiQuest/ChangeMe/g' {} +
find . -type f -name "*" -exec sed -i'' -e 's/Satoshiquest/Changeme/g' {} +
find . -type f -name "*" -exec sed -i'' -e 's/satoshiQuest/changeMe/g' {} +
find . -type f -name "*" -exec sed -i'' -e 's/SATOSHIQUEST/CHANGEME/g' {} +
shopt -s globstar
find . * | rename 's/satoshiquest/changeme/g'
shopt -s globstar
find . * | rename 's/satoshiquest/changeme/g'
shopt -s globstar
find . * | rename 's/SatoshiQuest/ChangeMe/g'
shopt -s globstar
find . * | rename 's/Satoshiquest/Changeme/g'
shopt -s globstar
find . * | rename 's/satoshiQuest/changeMe/g'
shopt -s globstar
find . * | rename 's/SATOSHIQUEST/CHANGEME/g'

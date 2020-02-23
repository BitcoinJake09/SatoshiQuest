find . -type f -name "*.*" -exec sed -i'' -e 's/devaultquest/devaultquest/g' {} +

find . -type f -name "*.*" -exec sed -i'' -e 's/DevaultQuest/DevaultQuest/g' {} +

find . -type f -name "*.*" -exec sed -i'' -e 's/Devaultquest/Devaultquest/g' {} +

find . -type f -name "*.*" -exec sed -i'' -e 's/devaultQuest/devaultQuest/g' {} +

find . -type f -name "*.*" -exec sed -i'' -e 's/DEVAULTQUEST/DEVAULTQUEST/g' {} +

shopt -s globstar

find . * | rename 's/devaultquest/devaultquest/g'

shopt -s globstar

find . * | rename 's/DevaultQuest/DevaultQuest/g'

shopt -s globstar

find . * | rename 's/Devaultquest/devaultQuest/g'

shopt -s globstar

find . * | rename 's/DEVAULTQUEST/DEVAULTQUEST/g'

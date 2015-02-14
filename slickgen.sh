color='\e[1;34m'
detail_color='\e[1;30m'
NC='\e[0m' # No Color
echo -e ${color}
echo "running slick code generation..."
echo -e ${detail_color}
cd ../slick-codegen
sbt slickGenerate
cd ../articlio
echo -e ${color}
echo "backing up current slick model..."
echo -e ${detail_color}
mv app/models/Tables.scala app/models/TablesOld.scala
echo -e ${color}
echo "copying new generated slick model..."
echo -e ${detail_color}
cp ../slick-codegen/target/scala-2.11/src_managed/articlio/models/Tables.scala app/models/
echo -e ${color}
echo "if no errors - done"
echo -e ${detail_color}



echo "Trying to stage and commit all changes"
echo -e ${detail_color}
git status
git add .
git add -u
git status
echo "Trying to commit with comment \"$1\""
echo -e ${color}
git commit -am "\"$1\""
echo
git status
echo -e ${NC}
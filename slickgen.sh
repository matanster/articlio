color='\e[1;34m'
detail_color='\e[1;30m'
NC='\e[0m' # No Color
echo -e ${color}"starting slick code generation to auto-create model from database..." ${detail_color}
cd ../slick-codegen
sbt slickGenerate
cd ../articlio
echo -e ${color}"backing up current model..." ${detail_color}
mv app/models/Tables.scala app/models/TablesOld.scala
echo -e ${color}"copying new generated slick model..." ${detail_color}
cp ../slick-codegen/target/scala-2.11/src_managed/articlio/models/Tables.scala app/models/
echo -e ${color}"if no errors - done" ${detail_color}
echo -e ${NC}
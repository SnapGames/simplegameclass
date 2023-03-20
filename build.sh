#!/bin/bash
#!/bin/sh
cd ./
export PROGRAM_NAME=simplegameclass
export PROGRAM_VERSION=1.0
export PROGRAM_TITLE=SimpleGameClass
export MAIN_CLASS=fr.snapgames.demo.core.Main
# paths
export SRC=./src
export LIBS=./lib
export TARGET=./target
export BUILD=$TARGET/build
export CLASSES=$TARGET/classes
export RESOURCES=$SRC/main/resources
# prepare target
rm -rf $TARGET
mkdir -p $CLASSES
# build manifest
echo "Build of program '$PROGRAM_NAME' ..."
echo "-----------"
echo "|_ 1. Create Manifest file '$TARGET/manifest.mf'"
echo 'Manifest-Version: $PROGRAM_NAME'>$TARGET/manifest.mf
echo "Main-Class: $MAIN_CLASS">>$TARGET/manifest.mf
echo "Implementation-Title: $PROGRAM_NAME">>$TARGET/manifest.mf
echo "Implementation-Version: build_N$GIT_COMIT_ID">>$TARGET/manifest.mf
echo "Implementation-Vendor: SnapGames">>$TARGET/manifest.mf
echo "Implementation-Author: fredericDOTdelormeATgmailDOTcom">>$TARGET/manifest.mf
echo "   |_ done"
# Compile class files
rm -Rf $CLASSES/*
echo "|_ 2. compile sources from '$SRC' ..."
find $SRC -name '*.java'  > $LIBS/sources.lst
javac @$LIBS/options.txt @$LIBS/sources.lst -cp $CLASSES
echo "   done."
# Build JAR
echo "|_ 3. package jar file '$PROGRAM_NAME.jar'..."
jar -cfmv $TARGET/$PROGRAM_NAME.jar $TARGET/manifest.mf -C $CLASSES . -C $RESOURCES .
echo "   |_ done."
# create runnable program
echo "|_ 4. create run file '$PROGRAM_NAME.run'..."
mkdir -p $BUILD
cat $LIBS/stub.sh $TARGET/$PROGRAM_NAME.jar > $BUILD/$PROGRAM_NAME.run
chmod +x $BUILD/$PROGRAM_NAME.run
echo "   |_ done."
echo "-----------"
echo "... '$PROGRAM_NAME' is built".

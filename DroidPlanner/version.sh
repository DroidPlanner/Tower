#/bin/bash

echo "Auto Version: `pwd`"

VERSION=`git describe --tag --dirty`

echo "   Ver:  ${VERSION}"

cat AndroidManifest.xml | \
    sed -e "s/android:versionName=\".*\"/android:versionName=\"${VERSION}\"/" \
    > bin/AndroidManifest.xml

exit 0

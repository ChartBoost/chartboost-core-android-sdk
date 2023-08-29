BRANCH_NAME=$1
VERSION=`echo $BRANCH_NAME | sed 's/release\///g'`
RELEASE_CANDIDATE="$VERSION-rc"

# Fetch from private artifactory all the versions matching RELEASE_CANDIDATE
curl -sSf -u "$JFROG_USER:$JFROG_PASS" -O \
    'https://cboost.jfrog.io/artifactory/private-chartboost-core/com/chartboost/chartboost-core-sdk/maven-metadata.xml'

# NEXT_VERSION gets the top rc and adds 1 to it. For example, 2.10.0-rc2 returns 3
NEXT_VERSION=`grep "$RELEASE_CANDIDATE" maven-metadata.xml | \
              sed "s/      <version>$RELEASE_CANDIDATE\([0-9][0-9]*\)<\/version>/\1/g" | \
              awk '{if ($1 > max) max = $1 } END {print max + 1}'`

# We print this out as output
echo "$RELEASE_CANDIDATE$NEXT_VERSION"

# Cleaning up that file we downloaded earlier
rm maven-metadata.xml

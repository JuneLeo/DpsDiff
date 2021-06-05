./gradlew :dependency-diff:build

if [ $1 -eq 1 ]; then
    ./gradlew :app:dps -s
else
   ./gradlew :app:dps2 -s > 1.txt
fi
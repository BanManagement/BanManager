logger.lifecycle("""
*******************************************
 You are building BanManager!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use gradlew and not gradle
 3) If you still need help, ask on Discord! Further information https://banmanagement.com/support

 Output files will be in [subproject]/build/libs
*******************************************
""")

plugins {
    id("io.freefair.aggregate-javadoc") version "8.11"
}

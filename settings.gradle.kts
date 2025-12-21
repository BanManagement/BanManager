rootProject.name = "BanManager"

include(":BanManagerCommon")
include(":BanManagerBukkit")
include(":BanManagerBungee")
include(":BanManagerSponge")
include(":BanManagerLibs")
include(":BanManagerVelocity")
include(":BanManagerFabric")
include(":BanManagerE2E")

project(":BanManagerCommon").projectDir = file("common")
project(":BanManagerBukkit").projectDir = file("bukkit")
project(":BanManagerBungee").projectDir = file("bungee")
project(":BanManagerSponge").projectDir = file("sponge")
project(":BanManagerLibs").projectDir = file("libs")
project(":BanManagerVelocity").projectDir = file("velocity")
project(":BanManagerFabric").projectDir = file("fabric")
project(":BanManagerE2E").projectDir = file("e2e")

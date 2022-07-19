[![](https://jitpack.io/v/DenuxPlays/JDA-ClanManager.svg)](https://jitpack.io/#DenuxPlays/JDA-ClanManager)
# JDA-ClanManager


A small system to manage clans, guilds or generally users.\
Mostly used for some privat projects.

## Installation
This version of DIH4JDA **must** be used with the following Version of JDA: [`net.dv8tion:JDA:5.0.0-alpha.15`](https://github.com/DV8FromTheWorld/JDA/releases/tag/v5.0.0-alpha.15)

### Maven

Add the [JitPack](https://jitpack.io/) repository to your `pom.xml`
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

Add the dependency
```xml
<dependency> 
    <groupId>com.github.DenuxPlays</groupId> 
    <artifactId>JDA-ClanManager</artifactId> 
    <version>1.0.0-alpha.5</version> 
</dependency>
```

### Gradle

Add the [JitPack](https://jitpack.io/) repository to your `build.gradle`
```gradle
repositories { 
    [...]
    maven { url "https://jitpack.io" } 
}
```

Add the dependency
```gradle
dependencies {
    [...]
    implementation("com.github.DenuxPlays:JDA-ClanManager:1.0.0-alpha.5")
}
```
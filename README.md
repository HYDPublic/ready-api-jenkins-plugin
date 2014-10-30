# Ready! API Jenkins Plugin

This is the Ready! API Jenkins Plugin code repository.

For help developing Jenkins plugins in general, see the [Jenkins plugin tutorial](https://wiki.jenkins-ci.org/display/JENKINS/Plugin+tutorial).

## To build:

```
mvn clean install
```

## To run:

### 1. Export MAVEN_OPTS (first run only):

* Unix

```
export MAVEN_OPTS="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n"
```

* Windows

```
set MAVEN_OPTS=-Xdebug -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
```

### 2. Run

```
mvn hpi:run
```

Or, to select some specific port for the Jenkins server to run on:

```
mvn hpi:run -Djetty.port=8090
```

To make the `hpi` prefix work, you need to add the following snippet to your `~/.m2/settings.xml`:

> if you don't want to do this, just type the fully qualified prefix: `org.jenkins-ci.tools:maven-hpi-plugin`

```xml
<settings>
  <pluginGroups>
    <pluginGroup>org.jenkins-ci.tools</pluginGroup>
  </pluginGroups>

  <profiles>
    <!-- Give access to Jenkins plugins -->
    <profile>
      <id>jenkins</id>
      <activation>
        <activeByDefault>true</activeByDefault> <!-- change this to false, if you don't like to have it on per default -->
      </activation>
      <repositories>
        <repository>
          <id>repo.jenkins-ci.org</id>
          <url>http://repo.jenkins-ci.org/public/</url>
        </repository>
      </repositories>
      <pluginRepositories>
        <pluginRepository>
          <id>repo.jenkins-ci.org</id>
          <url>http://repo.jenkins-ci.org/public/</url>
        </pluginRepository>
      </pluginRepositories>
    </profile>
  </profiles>
  <mirrors>
    <mirror>
      <id>repo.jenkins-ci.org</id>
      <url>http://repo.jenkins-ci.org/public/</url>
      <mirrorOf>m.g.o-public</mirrorOf>
    </mirror>
  </mirrors>
</settings>
```
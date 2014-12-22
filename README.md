# deploy-jenkins-dsl

[ ![Codeship Status for mateoconfeugo/deploy-jenkins-dsl](https://codeship.com/projects/ff88f2b0-6c3f-0132-eaf0-3a463caf9dbd/status?branch=master)](https://codeship.com/projects/53995)

A tool to move jobs created by the job-dsl https://github.com/jenkinsci/job-dsl-plugin
to the correct jenkins jobs directory

## Usage

instructions

```
git clone https://github.com/mateoconfeugo/deploy-jenkins-dsl.git
cd into that directory
edit the resources/config.edn file
in that deploy-jenkins-dsl dir
lein run
```

you can make an uberjar like
```
lein uberjar
java -jar target/deploy-jenkins-dsl-0.1.0-standalone.jar
```

you can make a little shell script or alias if you want


## License

Copyright © 2014 Matthew Burns 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.

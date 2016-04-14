## Spring Boot development Eclipse plugin

Eclipse plugin that helps with the development of Spring Boot. Some capabilities are:

 - Warns about `@Configuration` classes that use field injection
 - Warns about unnecessary use of `@Autowired` on classes with a single constructor
 - Quick fix for converting a configuration class from constructor injection to field injection

### Building

The plugin and its update site are built with Maven:

``` $ mvn clean package ```

### Installation

Once built, the plugin can be installed by using `io.spring.boot.development.eclipse.site.zip`
found in `io.spring.boot.development.eclipse.site/target/` as an archive update site.

### License

The plugin is open source software released under the [EPL 1.0 license][1]

[1]: https://www.eclipse.org/legal/epl-v10.html

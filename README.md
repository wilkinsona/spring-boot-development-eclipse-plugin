## Spring Boot development Eclipse plugin

Eclipse plugin that helps with the development of Spring Boot. Some capabilities are:

 - Warns about `@Configuration` classes that use field injection
 - Warns about unnecessary use of `@Autowired` on classes with a single constructor
 - Quick fix for converting a configuration class from constructor injection to field injection
 - Reports an error if a `FailureAnalyzer` is not listed in `META-INF/spring.factories`
 - Warns about the use of `@Component` in main code
 - Warns about public or protected functional interfaces in main code that are not annotated
   with `@FunctionalInterface`
 - Warns about single parameter lambda expressions that do not enclose the parameter in
   parentheses
 - Quick fix for enclosing a lambda expression's single parameter in parentheses
 - Warns about lambda expressions with a block body containing a single statement
 - Warns about unused method parameters in main code
 - Reports an error if a usage of AssertJ's `assertThat(…)` is incomplete

### Building

The plugin and its update site are built with Maven:

``` $ mvn clean package ```

### Using

Once built, the plugin can be installed by using `io.spring.boot.development.eclipse.site.zip`
found in `io.spring.boot.development.eclipse.site/target/` as an archive update site.

The plugin is enabled on a per-project basis. To enable it, select the project(s), right-click
and then click `Configure -> Enable Spring Boot Development builder`.

### License

The plugin is open source software released under the [EPL 1.0 license][1]

[1]: https://www.eclipse.org/legal/epl-v10.html

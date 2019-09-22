| CI                                  | Sonatype                                      | Issue Resolution                |
| ---                                 | ---                                           | ---                             |
| [![CI][Badge-Circle]][Link-Circle]  | [![Sonatype][Badge-Sonatype]][Link-Sonatype]  | [![Issue][Badge-IIM]][Link-IIM] |
# Zio-Delegate
This package defines an annotation and a typeclass that simplify working with mixins and proxies in scala.

## Installation
Add this dependency to your build.sbt
```scala
"dev.zio" %% "zio-delegate" % "0.0.5"
```
If using a scala version < 2.13 you'll also need to add the macro paradise compiler plugin.
```scala
compilerPlugin(("org.scalamacros" % "paradise"  % "2.1.1") cross CrossVersion.full)
```
If using scala 2.13 you need to add the macro annotation compiler option.
```scala
-Ymacro-annotations
```

## @delegate annotation
This annotation can only be used on a  constructur parameter in a class definition.
This will do a number of things to the resulting class definitions:

* The class will additionally extend any traits extended by the annotated member.
```scala
import zio.delegate._

trait Foo {
  def foo: Int = 4
}
object FooImpl extends Foo

class Bar(@delegate f: Foo)
val b: Foo = new Bar(FooImpl)
```

* Any methods on the resulting type of the defined class that are also defined on the annotated member will be forwarded to the member unless a definition exists in the body of the class.
```scala
import zio.delegate._

trait Foo {
  def foo: Int
}
abstract class Foo1 extends Foo {
  def foo = 4
  def foo1: Int
}

class Bar(@delegate f: Foo)
println(new Bar(new Foo {}).foo) // 4

class Bar1(@delegate f: Foo) {
  def foo = 3
}
println(new Bar1(new Foo {}).foo) // 3

// classes have to be explicitly extended. Forwarders will still
// be automatically generated though.
class Bar2(@delegate f: Foo1) exends Foo1
println(new Bar1(new Foo1 { def foo1 = 3 }).foo1) // 3
```

* The behavior of the annotation can be customized with three options
  ```scala
  class delegate(verbose: Boolean = false, forwardObjectMethods: Boolean = false, generateTraits: Boolean = true)
  ```
  - verbose: The generated class will be reported during compilation. This is very useful for debugging behavior of the annotation or getting a feel for the generated code.
  - forwardObjectMethods: controls whether methods defined on Object and similiar classes should be forwarded. The list of methods affected by this is currently:
  ```scala
  Set(
    "java.lang.Object.clone",
    "java.lang.Object.hashCode",
    "java.lang.Object.finalize",
    "java.lang.Object.equals",
    "java.lang.Object.toString",
    "scala.Any.getClass"
  )
  ```
  - generateTraits: Whether the class should be adopted to automatically extend any traits defined on the annotated member. If set to false only methods of traits / classes that are explicitly extended will be forwarded.

## Mix Typeclass

An instance of
```scala
trait Mix[A, B] {

  def mix(a: A, b: B): A with B

}
```
provides evidence that an instance of `B` can be mixed into an instance of `A`.
A macro is defined that can derive an instance of Mix for any two types if the first is a nonfinal class or a trait and the second one is a trait. It can be used like this
```scala
class Foo {
  def foo: Int = 2
}
trait Bar {
  def bar: Int
}
def withBar[A](a: A)(implicit ev: Mix[A, Bar]): A with Bar = {
  ev.mix(a, new Bar { def bar = 2 })
}
withBar[Foo](new Foo()).bar // 2
```
Definitions in the second type will override implementations in the first type.

## ZIO Modules
One of the primary motivations for writing this library was more comfortable incremental building
of ZIO environment cakes. A possible way of doing this is:
```scala
import zio.delegate._
import zio.blocking.Blocking
import zio.clock.Clock

trait Sys extends Serializable {
  def sys: Sys.Service[Any]
}
object Sys {
  trait Service[R] extends Serializable

  trait Live extends Sys { self: Clock with Blocking =>
    def sys = new Service[Any] {}
  }

  def withSys[A <: Clock with Blocking](a: A)(implicit ev: A Mix Sys): A with Sys = {
    class SysInstance(@delegate underlying: Clock with Blocking) extends Live
    ev.mix(a, new SysInstance(a))
  }
}
```
## Remarks
This is heavily inspired by [adamw/scala-macro-aop](https://github.com/adamw/scala-macro-aop) and [b-studios/MixinComposition](https://github.com/b-studios/MixinComposition). Make sure to check out the projects!

[Link-Circle]: https://circleci.com/gh/zio/zio-delegate "circleci"
[Link-Sonatype]: https://oss.sonatype.org/content/repositories/releases/dev/zio/zio-delegate_2.12/ "Sonatype Releases"
[Link-IIM]: https://isitmaintained.com/project/zio/zio-delegate "Average time to resolve an issue"

[Badge-Circle]: https://circleci.com/gh/zio/zio-delegate.svg?style=svg "circleci"
[Badge-Sonatype]: https://img.shields.io/nexus/r/https/oss.sonatype.org/dev.zio/zio-delegate_2.12.svg "Sonatype Releases"
[Badge-IIM]: https://isitmaintained.com/badge/resolution/zio/zio-delegate.svg "Average time to resolve an issue"


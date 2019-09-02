package com.schuwalow.delegate

class MixSpec extends UnitSpec {
  describe("Mix") {
    it("should allow mixing of traits") {
      trait Foo {
        def a: Int = 1
      }
      trait Bar {
        def b: Int = 2
      }
      val mixed = Mix[Foo, Bar].mix(new Foo {}, new Bar {})
      assert(mixed.a == 1 && mixed.b == 2)
    }
    it("should overwrite methods defined on both instances with the second") {
      trait Foo {
        def a: Int = 1
      }
      trait Bar extends Foo {
        override def a = 2
      }
      val mixed = Mix[Foo, Bar].mix(new Foo {}, new Bar {})
      assert(mixed.a == 2)
    }
    it("should allow the first type to be a class") {
      class Foo {
        def a: Int = 1
      }
      trait Bar {
        def b: Int = 2
      }
      val mixed = Mix[Foo, Bar].mix(new Foo(), new Bar {})
      assert(mixed.a == 1 && mixed.b == 2)
    }
    it("should support methods with same name") {
      trait Foo {
        def a(a: Int): Int
      }
      trait Bar {
        def a(a: String): String
      }
      val mixed = Mix[Foo, Bar].mix(new Foo { def a(a: Int) = 1 }, new Bar { def a(a: String) = "foo" })
      assert(mixed.a(1) == 1 && mixed.a("") == "foo")
    }
    it("should support type aliases") {
      trait Foo {
        def a(a: Int): Int
      }
      trait Bar {
        def a(a: String): String
      }
      trait Baz
      type FooBar = Foo with Bar
      val mixed = Mix[FooBar, Baz].mix(new Foo with Bar { def a(a: Int) = 2; def a(a: String) = "foo" }, new Baz {})
      assert(mixed.a(1) == 2 && mixed.a("") == "foo")
    }
    it("should support type aliases - 2") {
      trait Foo {
        def a(a: Int): Int
      }
      trait Bar {
        def a(a: String): String
      }
      trait Baz
      type FooBar = Foo with Bar
      val mixed = Mix[Baz, FooBar].mix(new Baz {}, new Foo with Bar { def a(a: Int) = 2; def a(a: String) = "foo" })
      assert(mixed.a(1) == 2 && mixed.a("") == "foo")
    }
  }
}

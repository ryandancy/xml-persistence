A simple library which persists Java classes to XML using annotations.

## An example

With the following code,
```java
@Persistable(toplevel=true, tag="foo", idField="fooId")
class Foo {
  
  private final String fooId = "TotallyUniqueID";
  
  @Persist("thisCoolObject") Bar myBar = new Bar();
  
  @Persist("thisCoolInteger") int myInt = 8;
  
}

@Persistable
class Bar {
  @Persist("whatsForDinner") String dinner = "Macaroni";
}
```

Calling `new XmlPersistor<>(Foo.class).toXml(new Foo())` will generate:

```xml
<?xml version="1.0" coding="utf-8"?>
<persisted>
  <foo root="true" id="TotallyUniqueID">
    <thisCoolObject>
      <whatsForDinner>Macaroni</whatsForDinner>
    </thisCoolObject>
    <thisCoolInteger>8</thisCoolInteger>
  </foo>
</persisted>
```

Calling `new XmlPersistor<>(Foo.class).fromXml(xml)` where `xml` is the above XML will
generate the original `Foo` object.

## Explanation

This library serializes all persistable classes in the hierarchy marked with `toplevel=true`
in a top-level element list called `<persisted>`. The root element is identified with the
attribute `root=true`. Non-`toplevel` elements are serialized inside the tag of their parent.

Note that no constructor will be called when regenerating an object, so make sure that any
fields *not* annotated `@Persist` can be reverted to their default values as if they were never
initialized at all - primitive types will be set to `0` and objects to `null`. (The `idField`
in toplevel objects will be set to the ID in the XML when regenerating.)

## API

### `@Persistable([boolean toplevel=false], [String name], [String idField])`

Marks a class that this library may persist.

If `toplevel` (default `false`) is `true`, then objects of this class will be serialized in the
top-level list of elements. In this case, the following attributes must be defined:

* `name` gives the name of the top-level tag
* `idField`, which gives the name of a field in the class holding a unique ID whose `toString()`
  method will be used to identify the element in the top-level list. The field must be a
  primitive, or must have a `fromString(String)` static method or a single String-argument
  constructor.

`toplevel=true` should be used when it is expected that the same object will be serialized
multiple times; otherwise, multiple identical objects will be re-generated instead.

### `@Persist(String value)`

Marks a (non-static) field that it is to be persisted. `value` gives the name of the tag in
the XML element representing this field. Only primitives, `String`s, and objects marked
`@Persistable` may be `@Persist`ed. (Support for collection types will be added later.)

### `XmlPersistor<R>`

A utility class containing methods which convert XML to Java objects and vice versa. Further
methods dealing with files may be added later. `R` is the type of the root objects which may
be persisted.

#### `XmlPersistor(Class<R> rootClass)`

Instantiate an `XmlPersistor`. `rootClass` is the class of the root objects which may be
persisted by this specific `XmlPersistor`. It must be `@Persistable(toplevel=true)`; if
not, `PersistenceException` is thrown.

#### `org.w3c.dom.Document toXml(R root)`

Persists an object, `root`, into an XML DOM `Document` which may be written to a file for
persistence. `root` must be `@Persistable(toplevel=true)`. `PersistenceException`, a
runtime exception, will be thrown if there are errors such as `@Persist`ing a
non-`@Persistable` class.

#### `R fromXml(org.w3c.dom.Document doc)`

Retrieves an `@Persistable(toplevel=true)` object of type `R` from the XML DOM document
`doc`. `RegenerationException` is thrown if there are errors in the XML document or if there
are mismatches such as the `root="true"` element of the document not being of type `R`.

### `PersistenceException`

A runtime exception, this is thrown if there are errors in persisting the class structure.

### `RegenerationException`

A checked exception, this is throw if there are errors in regenerating the class structure.

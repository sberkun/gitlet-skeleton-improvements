# Gitlet skeleton updates


As a student working on Gitlet in Spring 2021, I found the provided Utils.java file a little bit clunky. Hopefully the following changes will make debugging easier, for students and staff alike.


## Changes in Utils.java



### Replacing variadic function with single arg functions, and adding concat

The liberal use of variadic functions is a plague upon society, and also can by confusing for students who have never seen them before. In order to remedy this, all the variadic functions are now nice single arg functions, and the `concat` function has been added for when students need to write or hash multiple things together.

Note that this helps reduce an entire class of errors that I've seen students run into: putting the wrong type into variadic `Object...` functions, such as putting a Commit into sha1. The whole point of a type system is to eliminate this kind of error, so it's unfortunate that they were variadic in the first place.

Affected functions are:
 - `sha1(Object... vals)` has been replaced by `sha1(byte[] val)` and `sha1(String val)`.
 - `writeContents(File file, Object... contents)` has been replaced by `writeContents(File file, byte[] val)` and `writeContentsAsString(File file, String val)`.
 - `error(String msg, Object... args)` has been replaced by `error(String msg)`.

Remaining variadic functions are:
 - `concat(Object... vals)` hopefully has good enough error messages that it doesn't cause pain
 - `join(String first, String... others)` and `join(File first, String... others)` don't cause pain, so I'm letting them stay

### Removed several functions

These functions I felt were unnecessary, or at least didn't pull thier weight. Most of them are function overloads.
 - `sha1(List<Object> vals)`
 - `restrictedDelete(String file)` / `safeDelete(String file)`
 - `plainFilenamesIn(String dir)`
 - `message(String msg, Object... args)` (not an overload, but still unnecessary)

### Addition of a copyFile method

This is a basic enough operation that it should be provided to students, rather than making them look up how to do it themselves. 

### Changing restrictedDelete to safeDelete

The main difference is that safeDelete can also be used to delete files in the `.gitlet` folder, so students won't have to use `file.delete()` anywhere.

### Improved error messages

Now, most functions should give actual information about what's wrong, instead of "mUsT bE a nOrMaL fIlE".

Affected functions are:
 - `concat(Object... vals)` (new function)
 - `serialize(Serializable obj)` (and `writeObject(File file, Serializable obj)` as a result)
 - `safeDelete(File file)`
 - `copyFile(File source, File destination)` (new function)
 - `readContents(File file)`, `writeContents(File file, byte[] contents)` (and String equivalents as a result)
 - `readObject(File file, Class<T> expectedClass)`


### Changed plainFileNamesIn 

It now errors instead of returning null, since students should always be confidant that their file is a directory. If they're not, it's better to check file.isDirectory() rather than waiting for the NullPointerException.

Also, it returns an Array now instead of a List. If someone wants a List, they can turn it into a List themselves.

### Removed link annotations on join functions

Because Intellij is easily confused.

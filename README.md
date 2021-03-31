# Gitlet skeleton updates


As a student working on Gitlet in Spring 2021, I found the provided Utils.java file a little bit clunky. Hopefully the following changes will make debugging easier, for students and staff alike.


## Changes in Utils.java


#### replacing variadics with single arg functions, and adding concat (TODO)

The liberal use of variadics is a plaugue upon society, and also can by confusing for students who have never seen them before. In order to remedy this, all the variadic functions are now nice single arg functions, and the `concat` function has been added for when students need to write or hash multiple things together.

#### Improved error messages in sha1 (TODO)

TODO

#### Addition of a copyFile method (TODO)

This is a basic enough operation that it should be provided to students, rather than making them look up how to do it themselves. 

#### Changing restrictedDelete to safeDelete (TODO)

The main difference is that safeDelete can also be used to delete files in the `.gitlet` folder, so students will not be tempted to just use `file.delete()` everywhere.

#### Improved error messages in readContents (TODO)

TODO

#### Improved error messages in readObject (TODO)

TODO

#### plainFileNamesIn returns an array instead of a List (TODO)

Because why the heck did it return a List?

#### removed message function (TODO)

It is unnecessary.

#### removed link annotations on join function (TODO)

Because Intellij is easily confused.

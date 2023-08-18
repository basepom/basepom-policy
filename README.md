## basepom-policy

This is a policy jar that contains configuration and code

## Checker configuration

### checkstyle

Defines a very minimal code style:

- Do not check any generated sources (in `/generated-sources/` folders)

- Support Warning suppression with Annotations and comments (`SuppressWarningsFilter`, `SuppressWithNearbyCommentFilter`)
- require newline at end of file (`NewlineAtEndOfFile`)
- no spaces at end of line (`RegexpSingleline`)
- no hard tabs (`FileTabCharacter`)
- no wildcard imports (`AvoidStartImports`)
- no redundant imports (`AvoidRedundantImports`)
- java modifiers in JLS order (`ModifierOrder`)
- no dangeling statements for if()..., for()..., while()... (`NeedBraces`)
- no lowercase 'L' for long constants. (`UpperEll`)

### spotbugs

- suppress `THROWS_METHOD_THROWS_RUNTIMEEXCEPTION`, `THROWS_METHOD_THROWS_CLAUSE_THROWABLE` and `THROWS_METHOD_THROWS_CLAUSE_BASIC_EXCEPTION`; those were added in version 4.7 of spotbugs and cause a lot of noise in existing code bases that use this policy jar.

### license

- Add Apache license header without copyright line.

### shade

- contains the `CollectingManifestResourceTransformer`. It collects all the additional sections in shaded jars and includes them in the final fat jar.

----

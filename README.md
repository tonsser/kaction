KAction - a port of [SwiftCommunity/Action](https://github.com/RxSwiftCommunity/Action)
======


An **KAction** is a way to say "hey, later I'll need you to subscribe to this thing." It's actually a lot more involved than that.

KActions accept a `workFactory`: a closure that takes some input and produces an observable. When `execute()` is called, it passes its parameter to this closure and subscribes to the work.

- Can only be executed while "enabled" (`true` if unspecified).
- Only execute one thing at a time.
- Aggregates next/error events across individual executions.

Oh, and it has this really nice thing with `Button` that's pretty cool. It'll manage the button's enabled state, make sure the button is disabled while your work is being done, all that stuff 👍

_Works perfectly with [RxBinding](https://github.com/JakeWharton/RxBinding)_

Usage
-----

You have to pass a `workFactory` that takes input and returns an `Observable`. This represents some work that needs to be accomplished. Whenever you call `execute()`, you pass in input that's fed to the work factory. The `Action` will subscribe to the observable and emit the `Next` events on its `elements` property. If the observable errors, the error is sent as a `Next` even on the `errors` property. Neat.

Actions can only execute one thing at a time. If you try to execute an action that's currently executing, you'll get an error. The `executing` property sends `true` and `false` values as `Next` events.

```kotlin
val kaction = KAction<String, Boolean>(input, {
    networkLibrary.checkEmailExists(input)
})
...
kaction.execute("ash@ashfurrow.com")
```

Notice that the first generic parameter is the type of the input, and the second is the type of observable that `workFactory` creates. You can think of it a bit like the action's "output."

You can also specify an `enabledIf` parameter to the `KAction` initializer.
 
```kotlin
val isEmailValid: Observable<Boolean> = RxTextView.textChanges(emailTextView).map(this::isValidEmail())

val kaction = KAction<String, Boolean>(input, {
    networkLibrary.checkEmailExists(input)
}, isEmailValid)
```

Now `execute()` only does the work if the email address is valid. Super cool!

Note that `enabledIf` isn't the same as the `enabled` property. You pass in `enabledIf` and the action uses that, and its current executing state, to determine if it's currently enabled.

What's _really_ cool is the `Button` can be bound to the enabled state.

```kotlin
// The button executes the action 
button.setOnClickListener { kaction.execute(input) }

// But the enabled state is dependent on the action's state
kaction.enabled.subscribe { button.isEnabled = it }
```

Now when the button is pressed, the action is executed. The button's `enabled` state is bound to the action's `enabled` property. That means you can feed your form-validation logic into the action as a signal, and your button's enabled state is handled for you. Also, the user can't press the button again before the action is done executing, since it only handles one thing at a time. Cool.

##### Don't forget to check the [sample](https://github.com/tonsser/kaction/blob/master/sample/src/main/java/com/tonsser/kaction/sample/MainActivity.kt)


Download
--------

Add the dependency to your gradle file:

```groovy
implementation 'com.tonsser:kaction:0.0.1'
```

Special Thanks
--------------
Once again to the guys over [SwiftCommunity/Action](https://github.com/RxSwiftCommunity/Action) for the inspiration

## Author

**Joaquim Ley**, Android Engineer @ [Tonsser](https://github.com/tonsser)

- [![GitHub](https://raw.githubusercontent.com/tonsser/Cirque/master/img/GitHub.png) joaquimley](https://github.com/joaquimley)
- [![Twitter](https://raw.githubusercontent.com/tonsser/Cirque/master/img/Twitter.png) @joaquimley](https://twitter.com/joaquimley)


License
-------

    The MIT License
    
    Copyright (c) 2018 Tonsser
    
    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:
    
    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.
    
    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.
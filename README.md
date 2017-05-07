# AndroidSoftKeyboardListener
a detector for the android software keyboard's showing status. this implementation does not rely on window-size change. it uses edit-text focus & keyboard-close-detection. it seems to be behaving nicely with multi-windows as well.

   
#### to try it yourself you can either clone the repo or copy SoftKeyboardListener.java & ITracker.java & TrackedEditText.java (default edit-text augmented by a trivial interface) into an existing android project

---
this repository is a usage-sample
---

##### copied from the SoftKeyboardListener.java doc
```
/**
 * a keyboard listener that functions relying on customized edit-texts & their focus-state.<br>
 * this seems to be working nicely with multi-windows.<br><br>
 * this thing has limitations of course - see usage.
 *
 * usage:
 * <ol>
 *     <li>construct</li>
 *     <li>Acitivity.onCreate(..) -> this.onCreate(..)</li>
 *     <li>Acitivity.onCreate(..) -> this.addTracking(..) for all EditTexts you want
 *     managed by this listener</li>
 *     <li>Acitivity.onResume(..) -> this.onResume() necessary for
 *     restoring on orientation changes</li>
 *     <li>Tracked.requestFocus() to open the soft-keyboard for a specific view</li>
 *     <li>this.hideKeyboard() to hide the soft-keyboard if currently
 *     inputting in a tracked view</li>
 *     <li>DO NOT USE SHOW&HIDE THAT CAN BE FOUND IN InputMethodManager MANUALLY</li>
 * </ol>
 * (should also work in fragments)<br><br>
 *
 * how it works.<br>
 * the listener detects that a tracked edit-text has gained focus & in response
 * attempts to display the soft-keyboard, triggering the <i>execution of the callback</i>. <br>
 * when user attempts to close the keyboard, by pressing `back`, that key-event
 * is consumed & manual closing done, which triggers the <i>execution of the callback</i>. <br>
 * focus of a tracked view is an indication to this listener that the keyboard should be displayed
 * upon restoring of the state. <br><br>
 *
 * <span style="color: red;">known flaws: if initial focus is required, post-delay calling
 *  of the EditText.requestFocus() method (InputMethodManager.showSoftInput(...) returns `false`
 *  if it is called without a delay)</span>
 */
```

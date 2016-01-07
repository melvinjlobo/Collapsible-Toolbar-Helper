# Collapsible-Toolbar-Helper
A combination of custom CollapsibleToolbar and Collapsing Text to give you the same effect as App Bar Layout from the Desgin support library sans the use of a Toolbar
____
 Background:
  Although the new design support library allows us to do some cool stuff, the available sample code
  does not cover the potential that it has been built for. Most of the samples that I came across,
  including the cheese square example by Chris Banes, create the application so that minimum code has to be written.
  But what if I want a different layout and implementation? 
  My main requirement was that I did NOT require a Toolbar for my design. When going through the AOSP code for 
  CollapsibleTollbarLayout in the design support library, I realized that it has a tight integration with the Toolbar.
  So, Initially, I decided to work around that. There are a very few blogs out there that actually go deep into the actual potential 
  of the CollapsibleToolbarLayout. Few of them are listed below:
  
 - https://lab.getbase.com/introduction-to-coordinator-layout-on-android/ (Thanks Grzesiek Gajewski)
 - http://saulmm.github.io/mastering-coordinator/ (Thanks Saul Molinero).
  Saul has really explained the potential of the co-ordinator and AppBarLayout. For more   information,  read his post. He has also thrown in samples and managed to create some       
  complex layouts.
- https://gitlab.com/Sloy/collapsing-avatar-toolbar/blob/bf1c86c61021cb77bc28c1669475b116ef1a4e5a/library/src/main/java/com/sloydev/collapsingavatartoolbar/CollapsingAvatarToolbar.java (Thanks Rafa Vazquez)
 
  In a nutshell, it all boils down to a few things:
  1.   Coordinator Layout controls the "behavior" of its children which implement "CoordinatoLayout.Behavior"
       (http://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.Behavior.html)
       Check out NestedScrollView "Behavior" or AppBarLayout "Behavior" or FAB "Behavior" for implementations
  2.   AppBarLayout calls on a "OnOffsetChangedListener" implementation when it scrolls, which can be used
       to do stuff. Check out OnOffsetChangedListener in CollapsingToolbarLayout
 
  Interesting? Well, after drawing inspirations from the previously brilliantly written posts (Thanks again
  guys!), I decided to implement the AppBarLayout for this class (alpha animations for the children)
 
  I had another interesting idea for the Title (Thank you Mr. Chris Banes for the CollapsibleTitleLayout!)

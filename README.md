---------------------------
## Collapsible-Toolbar-Helper
----------------------------

A combination of custom CollapsibleToolbar and Collapsing Text to give you the same effect as App Bar Layout from the Desgin support library sans the use of a Toolbar

-----------
## ScreenShot
----------
 
![Screenshot](https://github.com/melvinjlobo/Collapsible-Toolbar-Helper/blob/master/CollabpsibleToolbarHelper.gif)

--------------
## BACKGROUND :
--------------

# CollapsibleToolbarHelper : 
Although the new design support library allows us to do some cool stuff, the available sample code  does not cover the potential that it has been built for. Most of the samples that I came across, including the cheese square example by Chris Banes, create the application so that minimum code has to be written.
 
**But what if I want a different layout and implementation?** 
My main requirement was that I did NOT require a Toolbar for my design. When going through the AOSP code for CollapsibleTollbarLayout in the design support library, I realized that it has a tight integration with the Toolbar. So, Initially, I decided to work around that. There are a very few blogs out there that actually go deep into the actual potential of the CollapsibleToolbarLayout. 

Few of them are listed below:
  
1. [Thanks Grzesiek Gajewski](https://lab.getbase.com/introduction-to-coordinator-layout-on-android/)
2. [Thanks Saul Molinero](http://saulmm.github.io/mastering-coordinator/)
3. [Thanks Rafa Vazquez](https://gitlab.com/Sloy/collapsing-avatar-toolbar/blob/bf1c86c61021cb77bc28c1669475b116ef1a4e5a/library/src/main/java/com/sloydev/collapsingavatartoolbar/CollapsingAvatarToolbar.java)
 
In a nutshell, it all boils down to a few things:
 
1. Coordinator Layout controls the "behavior" of its children which implement 
    [CoordinatoLayout.Behavior](http://developer.android.com/reference/android/support/design/widget/CoordinatorLayout.Behavior.html)
  Check out NestedScrollView "Behavior" or AppBarLayout "Behavior" or FAB "Behavior" for implementations
  
2. AppBarLayout calls on a [OnOffsetChangedListener](http://developer.android.com/reference/android/support/design/widget/AppBarLayout.OnOffsetChangedListener.html) implementation when it scrolls,   
   which can be used to do stuff. Check out OnOffsetChangedListener in 
   CollapsingToolbarLayout

Well, that was my premise of creating the CollapsibleToolbarHelper. It of course is derived heavily from the AOSP code, but with a few modifications. It supports the following collapse modes:
	- MOVE_ON_SCROLL = The children move when the collapsible toolbar collapses to hit the bottom of that view
    - PARALLAX_ON_SCROLL = THe usual parallax, move and then hide when the Toolbar collapses
    - PIN_ON_SCROLL = Stays at the position where it is laid out originally
    - HIDE_ON_COLLAPSE = Stays at its location and hides when the toolbar is collapsed
    - SHOW_ON_COLLAPSE = Is hidden at the location that it is supposed to and appears
    after the Toolbar has collapsed
**Note that it has to be used as a direct child of AppBarLayout since it depends on the OffsetChangedListener of the AppBarLayout to collapse and expand.**

# CollapsibleTextLayout :
  Since I was designing the CollapsibleToolbar, I required a CollapsibleText as well. But the default CollapsibleTextHelper is coupled too tightly with the CollapsingToolbarLayout and not public to be modified. So, taking inspirations
from AOSP, Chris banes' [Philm](https://github.com/chrisbanes/philm/blob/master/app/src/main/java/app/philm/in/view/CollapsingTitleLayout.java) and Nick Butchers [Plaid](https://github.com/nickbutcher/plaid/blob/master/app/src/main/java/io/plaidapp/util/CollapsingTextHelper.java), I wrote my own CollapsingTextlayout.

I know that there is a lot of room for improvement and that there are obvious optimizations and feature enhancements, but the main objective of re-writing these widgets was learning. I have collected all the ideas from the given references and tried to simplify the implementation as much as possible. The code is commented and should be self explanatory. 

Interested? Well go ahead, download and use the widgets.
_______

# Usage
```xml
<android.support.design.widget.CoordinatorLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:fitsSystemWindows="true">

    <android.support.design.widget.AppBarLayout
        android:id="@+id/app_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:paddingTop="5dp"
        android:paddingBottom="30dp"
        android:theme="@style/AppTheme.AppBarOverlay"
        >

        <abysmel.com.collapsibletoolbarhelper.widgets.CollapsibleToolbarHelper
            android:id="@+id/toolbar_layout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            android:gravity="center_vertical"
            android:layout_marginTop="10dp"
            android:background="@android:color/transparent"
            >

	        <!--Note that the position has to be set with respect to the collapsed toolbar. So,
	            aligning it in center initially (centerInParent="true") will NOT work as it will remain
	            at its position even after the Toolbar collapses. Also note that the initially set the
	            alpha of the view to zero and NOT the visibility to gone, as the view will then have never
	            been drawn which will throw all calculations haywire for show_on_collapse. Wish there
	            was a better way to do this-->
            <ImageView
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:id="@+id/hello.img"
	            android:src="@mipmap/hello"
	            android:layout_marginLeft="10dp"
	            android:alpha="0"
                app:layout_marginTopPercent = "3%"
                app:collapseMode="show_on_collapse"/>

            <!-- Title -->
            <abysmel.com.collapsibletoolbarhelper.widgets.CollapsibleTextLayout
                android:id="@+id/hello.text"
                android:layout_height="wrap_content"
                android:layout_width="wrap_content"
                app:layout_widthPercent="59%"
                android:layout_alignParentLeft="true"
                app:layout_marginTopPercent = "1%"
                app:layout_marginBottomPercent = "1%"
                app:layout_marginLeftPercent="5%"
                android:textColor="@android:color/white"
                app:collapseMode="pin_on_scroll"
                app:textToShow = "Hello World"
                app:expandedTextColor = "@android:color/white"
                app:collapsedTextColor = "@android:color/white"
                app:typefaceFamilyPrimary = "sans-serif-light"
                app:typefaceFamilySecondary = "sans-serif-medium"
                app:isMultiFaceted="true"
                app:typefaceSplitPosition="5"
                app:expandedTextSize = "62dp"
                app:collapsedTextSize = "32dp"
                app:maxExpandedTextSize = "62dp"
                />

            <!-- The hello number edit button -->
            <ImageView
                android:id="@+id/hello.edit"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_alignParentRight="true"
                app:layout_marginTopPercent = "3%"
                app:layout_marginBottomPercent = "1%"
                app:layout_marginRightPercent = "4%"
                android:layout_marginLeft="10dp"
                android:src="@mipmap/pencil"
                app:collapseMode="hide_on_collapse"/>

            <!-- The hello Number -->
            <TextView
                android:id="@+id/hello.number"
                android:layout_height="wrap_content"
                android:layout_width="wrap_content"
                app:layout_widthPercent="40%"
                app:layout_marginTopPercent = "1%"
                app:layout_marginBottomPercent = "1%"
                app:layout_marginRightPercent = "1%"
                android:layout_toLeftOf="@id/hello.edit"
                android:layout_toRightOf="@id/hello.text"
                android:gravity="end"
                android:text="@string/hello_no_sign"
                android:textColor="@android:color/white"
                android:textSize="@dimen/hello_no_size"
                app:collapseMode="pin_on_scroll"/>



            <!-- Version -->
            <TextView
                android:id="@+id/version"
                app:layout_widthPercent="40%"
                android:layout_height="wrap_content"
                android:layout_width="wrap_content"
                android:layout_below="@id/hello.text"
                android:layout_marginTop="5dp"
                android:layout_alignParentLeft="true"
                app:layout_marginLeftPercent="5%"
                android:text="@string/version"
                android:textColor="@android:color/white"
                android:textSize="@dimen/version_no_size"
                app:collapseMode="parallax_on_scroll"
                 />

            <!-- E-mail Address -->
            <TextView
                android:id="@+id/hello.address"
                app:layout_widthPercent="40%"
                android:layout_height="wrap_content"
                android:layout_width="wrap_content"
                android:layout_below="@id/hello.number"
                android:layout_alignParentRight="true"
                android:gravity="end"
                app:layout_marginRightPercent="5%"
                android:text="@string/emailaddress"
                android:textColor="@android:color/white"
                android:textSize="@dimen/mail_address_size"
                app:collapseMode="parallax_on_scroll"
                 />
        </abysmel.com.collapsibletoolbarhelper.widgets.CollapsibleToolbarHelper>
    </android.support.design.widget.AppBarLayout>
</android.support.design.widget.CoordinatorLayout>
```
-------
## REFERENCES
--------
- [Philm](https://github.com/chrisbanes/philm/blob/master/app/src/main/java/app/philm/in/view/CollapsingTitleLayout.java)
- [Plaid](https://github.com/nickbutcher/plaid/blob/master/app/src/main/java/io/plaidapp/util/CollapsingTextHelper.java)
- [AutoFit TextView](https://github.com/grantland/android-autofittextview)
-  [Grzesiek Gajewski](https://lab.getbase.com/introduction-to-coordinator-layout-on-android/)
- [Saul Molinero](http://saulmm.github.io/mastering-coordinator/)
- [Rafa Vazquez](https://gitlab.com/Sloy/collapsing-avatar-toolbar/blob/bf1c86c61021cb77bc28c1669475b116ef1a4e5a/library/src/main/java/com/sloydev/collapsingavatartoolbar/CollapsingAvatarToolbar.java)

----------
## LICENSE
-------
 
The MIT License (MIT)
 
Copyright (c) 2016 Melvin Lobo
 
Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:
 
The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.
 
THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

____

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Collapsible%20Toolbar-green.svg?style=flat)](https://android-arsenal.com/details/1/3001)

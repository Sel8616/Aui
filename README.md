![Aui](https://git.oschina.net/sel/R/raw/master/img/aui_logo.png)

#Aui

###⿻ About
***
**SwipeAdapterView** = SwipeRefreshLayout + GridView + FooterBar + RockerButton

**PictureView** = A image view that can display image from a remote url.

**ViewLooper** = A simple looping viewpager.

**RockerButton** = A floating button which can be dragged toward 4 directions. Rocker means 'Jointed arm'.

[--> Download aar <--](https://git.oschina.net/sel/R/raw/master/lib/Aui.aar "Download aar")

[--> Download demo APK <--](https://git.oschina.net/sel/R/raw/master/demo/Aui_Demo.apk "Download demo APK")


###♨ Features
***
####☞ SwipeAdapterView
* Data Loading

Refresh -- Pull it down when the GridView is scrolled to the top.

LoadMore -- Click the FooterBar.

* Data View(LIST/GRID)

Display data in a ListView or GridView. Actually, this 'ListView' is also a GridView which has only one column.

* FooterBar(DOCKED/SCROLLING/DISABLED)

Show messages related with data loading. It has 3 modes: Docked at the bottom/Pinned in the end of data list/Disabled.

* RockerButton(Docked on the bottom-right corner)

Click the button to scroll back to the top.

Drag the button toward 4 directions: LEFT-Previous Page. RIGHT-Next Page. UP-Scroll up. DOWN-Scroll down.

* TranscriptMode(NORMAL/ALWAYS_SCROLL/DISABLED)

Synonym for AbsListView.TranscriptMode

* FloatingButton(ENABLED/DISABLED)

Click to scroll back to the top quickly

####☞ PictureView
* Display android.Drawable or image from local/remote file. 

####☞ ViewLooper
* Dynamically add or remove a page. 

* Switch the pages by scroll or fling actions. 

####☞ RockerButton
* Drag toward 4 directions.

* Specific different RockerMode for each direction.

* RockerMode: ONCE / ELASTIC

* 5 Callbacks: onLeft(float traction)/onRight(float traction)/onUp(float traction)/onDown(float traction)/onStop(@Nullable Direction direction)

* The further it is dragged, the larger the traction is.

* During one MotionEvent's lifecycle, current direction callback will be called every time when ACTION_MOVE happened in ELASTIC mode, while only once in ONCE mode.


###☀ Notice
***
● This is an AndroidStudio project.

● SDK level requirement is 15-23.


###☹ Problems
***
● Stupid GridView [Issue](https://github.com/Sel8616/Aui/issues/1)


###☺ Contact
***
✉  sel8616@gmail.com    philshang@163.com

Ⓠ  117764756

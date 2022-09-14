# Layout for Java

> A simple/fast stacking box layout library. It's useful for calculating layouts for things like 2D user interfaces.

This is a port of [randrew's layout](https://github.com/randrew/layout) rewritten in java, with some java specific additions. 
It can be used as a layouting engine and serve as a foundation for UI in your applications or games.

![layout-demo](https://user-images.githubusercontent.com/8457835/189464350-4f53ccdb-67ff-465a-b927-d76f9c2dc508.gif)

## How to Try

Demo code is in `demo` module within the root directory. You can build and try it yourself by following these steps:

```
git clone https://github.com/abvadabra/layout-java.git
cd layout-java
./gradlew :demo:run
```

### How to use

This library is published to maven central. Alternatively, you can download jar file from [Releases](https://github.com/abvadabra/layout-java/releases)

#### Gradle
```
implementation("io.github.abvadabra:layout-java:1.2.0")
```
#### Maven
```
<dependency>
  <groupId>io.github.abvadabra</groupId>
  <artifactId>layout-java</artifactId>
  <version>1.2.0</version>
  <type>module</type>
</dependency>
```

### Example

API is provided in two slightly different forms, although they both work the same way. One resembles original C-style API with all actions performed 
via static methods in `Layout` class, and another is a bit more suitable for usage in java with all actions executed via `LayoutContext` instance.
Example below demonstrates usage the latter.
```java
// Let's pretend we're creating some kind of GUI with a master list on the                    
// left, and the content view on the right.                                                   
                                                                                              
// First we need to create a context                                                          
LayoutContext ctx = new LayoutContext();                                                      
                                                                                              
// The context will automatically resize its heap buffer to grow as needed                    
// during use. But we can avoid multiple reallocations by reserving as much                   
// space as we'll need up-front. Aside from items creation layout won't perform any           
// other heap allocations in runtime.                                                         
ctx.reserveItemsCapacity(1024);                                                               
                                                                                              
// Create our root item. Items are just 2D boxes which are identified by simple integers.     
int root = ctx.item();                                                                        
                                                                                              
// Let's pretend we have a window in our game or OS of some known dimension.                  
// We'll want to explicitly set our root item to be that size.                                
ctx.setSize(root, 1280, 720);                                                                 
                                                                                              
// Set our root item to arrange its children in a row, left-to-right, in the                  
// order they are inserted.                                                                   
ctx.setContain(root, LayoutBoxFlags.LAY_ROW);                                                 
                                                                                              
// Create the item for our master list.                                                       
int masterList = ctx.item();                                                                  
ctx.insert(root, masterList);                                                                 
// Our master list has a specific fixed width, but we want it to fill all                     
// available vertical space                                                                   
ctx.setSize(masterList, 400, 0);                                                              
// We set our item's behavior within its parent to desire filling up available                
// vertical space.                                                                            
ctx.setBehave(masterList, LayoutFlags.LAY_VFILL);                                             
// And we set it so that it will lay out its children in a column,                            
// top-to-bottom, in the order they are inserted.                                             
ctx.setContain(masterList, LayoutBoxFlags.LAY_COLUMN);                                        
                                                                                              
int contentView = ctx.item();                                                                 
ctx.insert(root, contentView);                                                                
// The content view just wants to fill up all of the remaining space, so we                   
// don't need to set any size on it.                                                          
//                                                                                            
// We could just set LAY_FILL here instead of bitwise-or'ing LAY_HFILL and                    
// LAY_VFILL, but I want to demonstrate that this is how you combine flags.                   
ctx.setBehave(contentView, LayoutFlags.LAY_HFILL | LayoutFlags.LAY_VFILL);                    
                                                                                              
// Normally at this point, we would probably want to create items for our                     
// master list and our content view and insert them. This is just a dumb fake                 
// example, so let's move on to finishing up.                                                 
                                                                                              
// Run the context -- this does all of the actual calculations.                               
ctx.runContext();                                                                             
                                                                                              
// Now we can get the calculated size of our items as 2D rectangles.                          
// Function layGetRect expects array of 4 elements where rectangle array will be written to.  
// The four components of the array represent x and y of the top left corner, and then        
// the width and height.                                                                      
float[] masterListRect = ctx.getRect(masterList, new float[4]);                               
float[] contentViewRect = ctx.getRect(contentView, new float[4]);                             
                                                                                              
// masterListRect  = {   0, 0, 400, 720 }                                                     
// contentViewRect = { 400, 0, 880, 720 }                                                     
                                                                                              
// If we're using an immediate-mode graphics library, we could draw our boxes                 
// with it now.                                                                               
drawBox(masterListRect[0], masterListRect[1], masterListRect[2], masterListRect[3]);          
                                                                                              
// After you've used layRunContext, the results should remain valid unless a                  
// reset occurs.                                                                              
//                                                                                            
// However, while it's true that you could manually update the existing items                 
// in the context by using laySetSize{_xy}, and then calling layRunContext                    
// again, you might want to consider just rebuilding everything from scratch                  
// every frame. This is a lot easier to program than tedious fine-grained                     
// invalidation, and a context with thousands of items will probably still only               
// take a handful of microseconds.                                                            
//                                                                                            
// There's no way to remove items -- once you create them and insert them,                    
// that's it. If we want to reset our context so that we can rebuild our layout               
// tree from scratch, we use layResetContext:                                                 
ctx.resetContext();                                                                           
                                                                                              
// And now we could start over with creating the root item, inserting more                    
// items, etc. The reason we don't create a new context from scratch is that we               
// want to reuse the item objects which were already allocated.                                                        
```


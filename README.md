<h1>HaMeR - Handler, Message & Runnable</h1>
<h3>A sample application on how to use the Android HaMeR framework</h3>

<p>
The code is fully commented and illutrates how to post/send Runnable and Message objects between Threads and how to prepare a Thread using HandlerThread.
</p>
If you want to learn more, check this <a href="http://code.tutsplus.com/tutorials/concurrency-on-android-using-hamer-framework--cms-27129">tutorial about the HaMeR</a> framework.

<h4>The app is composed by:</h4>
<ul>
<li>Two Activities, one for Runnable e other for Message calls</li>
<li>Two HandlerThread objects</li>
<ul>
<li>WorkerThread to receive and process calls from the UI</li>
<li>CounterThread to receive Message calls from the WorkerThread</li>
</ul>
<li>And some utility classes and layout resources</li>
<li>The app has a simple UI with buttons to download some images and start a counter</li>
</ul>

<h4>Posting and receiving Runnables with <a href="https://github.com/tinmegali/hamer/blob/master/app/src/main/java/com/tinmegali/hamer/RunnableActivity.java">RunnableActivity</a></h4>
<ol>
<li>The RunnableActivity instantiate a background Thread called WorkerThread passing a Handler and a WorkerThread.Callback as parameters
<li>The activity makes a call on WorkerThread asking it to download a image. 
<li>The download is done asynchronously on the background thread
<li>The WorkerThread sends the image to the RunnableActivity posting a Runnable to it using a Handler that it received as parameter earlier.
</ol>

<h4>Sending and Receiving Messages with <a href="https://github.com/tinmegali/hamer/blob/master/app/src/main/java/com/tinmegali/hamer/MessageActivity.java">MessageActivity</a></h4>
<ol>
<li>The MessageActivity instantiate a background Thread calledWorkerThread passing a Handler as parameters
<li>The activity makes a call on WorkerThread asking it to download a specific image or download a random one. 
<li>The download is done asynchronously on the background thread
<li>The WorkerThread  create a Message using the 'key' defined on the MessageActivity and passing the image downloaded as a parameter object.
<li>The Message in sent to the MessageActivity using a Handler that WorkerThread received as parameter earlier.
<li>The Handler on MessageActivity processes the Message, get the object from it and exhibits on the UI
</ol>

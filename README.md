**Author** Claudia Moreno Bernal
**StudentId** w1894295

---


### Base URL


http://localhost:8080/SmartCampus/api/v1


---

## Answers to the Report Questions

### Part 1: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton	q``q? Elaborate on how this architectural decision impacts the way you manage and synchronize your in-memory data structures (maps/lists) to prevent data loss or race conditions

By default, JAX-RS creates a new instance of a resource class for every single HTTP request. When the request finishes, that instance gets thrown away. This keeps things simple because each request has its own isolated object, so threads don't interfere with each other.

The problem is that if I stored my rooms in a normal instance field, they'd disappear the moment the request ended. To keep the data alive between requests, I made the maps static. Static fields belong to the class itself, not to any one instance, so they stick around for the whole time the app is running.

That fixes the data loss, but it creates a new issue: multiple requests might try to read or write to the same HashMap at the same time, since Tomcat runs each request on its own thread. HashMap isn't thread-safe, so in theory two writes at the same time could corrupt it. In a real system I'd swap it for ConcurrentHashMap or wrap the tricky parts in synchronized blocks. For this coursework the brief allows plain `HashMap` and the marking is single-user, so I kept it simple.

---

### Part 1: Why is the provision of ”Hypermedia” (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?

HATEOAS means the server tells the client where to go next by including links in the response, instead of making the client memorise every URL. My Discovery endpoint does a small version of this: it returns a map pointing to /api/v1/rooms and /api/v1/sensors, so a client that only knows the root URL can figure out the rest.

The main benefit for client developers is that they don't have to hardcode URLs. If the server changes its URL structure later, the client still works as long as it follows the links the server gives it. Static documentation, on the other hand, gets out of date the moment someone changes the code, and developers usually only find out when something breaks.

It also lets the server hide things the client can't do right now. For example, if a sensor is in MAINTENANCE, the response could leave out the "add reading" link, so the client knows not to try it without needing to understand the business rule itself.

---

### Part 2: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client side processing.

Returning full objects is easier for the client. They get everything they need in one request and don't have to make extra calls. For a small dataset like mine, it's the better choice, which is why GET /rooms returns the whole room objects.

Returning just IDs makes sense when you've got a huge number of rooms or each object carries a lot of data. A response with thousands of full rooms could be megabytes in size, which means slow load times, more mobile data used, and the client throwing away most of the info anyway if the user only wanted names.

A middle ground is to return small summaries in the list (like ID and name), and make the client call /rooms/{id} if it wants the full details. That keeps the list small but means more round-trips. Which one to pick really depends on what the clients actually do, my API goes with the simple "return everything" approach because the dataset is tiny.

---

### Part 2: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.

Idempotent means that doing the same request more than once has the same effect on the server as doing it once. My DELETE is idempotent in the way that matters.

The first time I send DELETE /rooms/LAB-101 (assuming it has no sensors), the room gets removed and the server responds with 200 OK. If I send the exact same request again, the room is already gone, so the server returns 404 Not Found. Same if I send it a third or fourth time the response stays 404 and nothing else changes on the server.

The key thing is: the state of the server is the same after one call as it is after ten calls. The room is gone, and no extra damage is done. The response code changes (200 to 404), but idempotency is about side effects, not about the exact response. That's why DELETE is classed as idempotent in HTTP a client can safely retry a DELETE without worrying about deleting something twice.

---

### Part 3: We explicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?

When a method has @Consumes(MediaType.APPLICATION_JSON), Jersey only accepts requests with Content-Type: application/json. If a client sends text/plain or application/xml instead, Jersey rejects the request before the method even runs, and returns 415 Unsupported Media Type.

This is handy because the method itself doesn't have to check the format by the time the code runs, Jersey has already confirmed the body is JSON. It's also fast: the request gets rejected straight away instead of the server trying to parse something that was never going to work.

The same thing happens in reverse with @Produces. If the client sends an Accept header that doesn't match what the method can produce, Jersey returns 406 Not Acceptable.

---

### Part 3: You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (a.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?

Putting the filter in the path like /sensors/type/CO2 makes it look like CO2 is a separate resource nested under sensors, which isn't really what it is. The actual resource is the list of sensors, CO2 is just a filter on that list. /sensors?type=CO2 says that more clearly that the sensors, filtered to type CO2.

The bigger advantage shows up when you want to filter by more than one thing. With query params, I can do /sensors?type=CO2&status=ACTIVE&roomId=LIB-301 and the same method handles all of it. With path segments, I'd have to write loads of different URL patterns to cover every combination, or force the client to put placeholders in when they only want some filters.

Query params are also optional by default. If no filter is sent, @QueryParam gives me null and I just return everything, which is exactly what should happen. With path segments I'd have to define a separate route for "no filter." Plus, caches and logging tools are designed to understand query params as filters, whereas treating every filter path as a different resource can confuse caching.

---

### Part 4: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive controller class?

If I didn't use sub-resource locators, my SensorResource class would have to handle everything: listing sensors, getting one sensor, listing readings, adding readings, and any future nested stuff like alerts. That class would turn into a mess pretty quickly, with lots of unrelated methods crammed into the same file.

With the locator pattern, SensorResource has one method that says "anything under /{sensorId}/readings goes to SensorReadingResource." From that point on, everything to do with readings lives in its own class, creating readings, fetching the history, updating the parent sensor's currentValue. SensorResource doesn't know or care about any of that.

The benefits are the usual ones for breaking code into smaller pieces: each class stays short and easy to read, changing reading logic doesn't affect sensor logic, and adding a new sub-resource (like alerts) is a one-line addition plus a new class, no surgery on existing code. In a big API with lots of nested routes, this makes the difference between a codebase that stays manageable and one that turns into a monster controller.

---

### Part 5: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?

The two codes answer different questions. 404 Not Found means the URL you asked for doesn't exist. 422 Unprocessable Entity means the URL was fine, the body was valid JSON, but what's in it can't be used, something it refers to doesn't exist.

When someone POSTs a new sensor to /sensors with "roomId": "GHOST-ROOM"`, the URL /sensors is perfectly valid, it's the right endpoint for creating sensors. Returning 404 would be misleading because it would suggest /sensors itself is wrong, which would send the client hunting in the wrong direction. 422 says more honestly: "you hit the right endpoint, I understood your JSON, but the room you're referencing doesn't exist."

It also keeps 404 free for its real meaning. If the same client later does GET /rooms/GHOST-ROOM, a 404 is the right answer because the URL genuinely points to something that doesn't exist. Using each code for what it actually means keeps the API predictable.

---

### Part 5: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?

A Java stack trace is meant for developers, and it assumes you trust the people reading it. If it gets sent to an external client, it gives attackers a lot of useful information.

A trace shows the full package structure and class names (like com.mycompany.smartcampus.resources.RoomResource), which tells an attacker how my code is laid out. It shows which libraries I'm using and often their exact versions, if any of those have known security issues, the attacker now knows where to start. It can also leak file paths, the Java version, and server details, all of which narrow down which attacks to try.

On top of that, traces give away logic. A NullPointerException in RoomResource.getRoomById with a line pointing at rooms.get(roomId) tells the attacker I'm using a Map. None of this is a disaster on its own, but in security every small detail helps the attacker and hurts the defender.

The fix is to translate every error into a clean, generic JSON response at the API boundary. My GlobalExceptionMapper does this, it catches anything unexpected and returns a 500 with no stack trace. The real trace still gets logged on the server where only I can see it, so debugging still works, but the outside world doesn't get to read it.

---

### Part 5: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?

If I put Logger.info(...) inside every method, I'd be writing the same code over and over, and if I ever wanted to change the log format I'd have to edit all of them. Plus, it's easy to forget to add logging to a new endpoint, so the app's observability quietly gets worse as it grows.

A JAX-RS filter solves this by putting the logging in one place. My LoggingFilter implements both ContainerRequestFilter and ContainerResponseFilter, and because it's marked @Provider, it runs automatically for every request and every response. New endpoints get logging for free. Deleting an endpoint doesn't leave broken log statements behind. Changing the log format means editing one file.

Filters also have access to things that are hard to get at from inside a method, the full request context, the response status, all the headers. For what the coursework wanted (logging the method, URI, and response status), a filter is the cleanest way to do it.

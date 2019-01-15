#Contribution Guidelines
**Firstly, thanks for using the ORS and contributing to making it better for everyone.**

OpenRouteService has been around for over ten years, and as such it has seen many changes and adoption of new methodologies and techniques, as well as the coming and going of multiple developers. To try and keep the OpenRouteService codebase clean and stable, there are a few guidelines that we try to follow.
###Creating issues
When creating an issue on GitHub, please complete as many of the following steps as possible to ensure that we can better locate the problem:
* Provide a description of what should happen and what does happen
* If the problem is with an API request, provide the URL that you used (ommitting your API key)
* If the problem is related to geometry, provide a screenshot of the route

We aim to do a minor update at the end of every month which will add any bug fixes to the live ORS.

###Creating Pull Requests
When creating pull requests, ensure that you address each item in the checklist before submitting. When an item is not applicable, leave it blank or better yet, use a pait of ~ on each side of the item to show that it is not applicable.
**Only create pull requests against the development branch!**

###Contributing code
Though there are no concrete rules for code that is contributed, we have a few general styles that should be adopted:
* Always make your code easy to read - methods, variables and classes should all have sensible names that tell you what they are for
* Comments should be used to explain something that needs some background and for JDocs descriptions. They should *NEVER* be used to comment out blocks of code! In general, following the principle of proper naming for variables and methods, your code should be self explanitory and so not need an abundance of comments to explain it.
* Try to make your code extendable with OO techniques (i.e. interfaces) where possible
* Write unit tests
* If your code adds new functionality to the API, make sure to write corresponding API-Tests in the openrouteservice-api-tests subproject.
* Try to keep methods short - it is is almost always better to have multiple short methods that do one thing each rather than one goliath of a method that tries to do everything.
* Keep an eye out for an elusive yet mischievous digital llama hiding in the code - reports say that he appears every now and then and changes a double to a float, just to make things difficult... but then again, maybe a developer created this rumour to cover up their mistakes... no one knows for sure
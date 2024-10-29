# `ors.engine.graph_management`

Properties beneath `ors.engine.graph_management` are used to define graph management for the entire openrouteservice instance.
See [graph repo client](/technical-details/graph-repo-client/) for more information.

| Property              | Meaning                                                                      | Default                 |
|-----------------------|------------------------------------------------------------------------------|-------------------------|
| `enabled`             | globally enable or disable graph management for all profiles                 | `false`                 | 
| `download_schedule`   | cron pattern defining the schedule when to check for and download new graphs | a pattern meaning never |
| `activation_schedule` | cron pattern defining the schedule when to activate downloaded new graphs    | a pattern meaning never |
| `max_backups`         | how many old graph version to keep as backup when activating a new one       | `0`                     |

#### Cron Patterns

The values for the `*_schedule` properties are cron patterns with 6 positions.
The positions are interpreted as follows (from left to right):

* second
* minute
* hour
* day of month
* month
* day of week

For more information see [org.springframework.scheduling.annotation](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/scheduling/annotation/Scheduled.html#cron())

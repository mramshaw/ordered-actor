# ordered-actor

[![Known Vulnerabilities](https://snyk.io/test/github/mramshaw/ordered-actor/badge.svg?style=plastic&targetFile=build.sbt)](https://snyk.io/test/github/mramshaw/ordered-actor?style=plastic&targetFile=build.sbt)

An example of how to ensure ordered message processing with AKKA actors

## Motivation
This was a fun exercise. I planned to get some hands-on with AKKA, Scala and sbt - but then it seemed like a good opportunity to use sbt and hack on some Scala. Specifically I added a run-time option to specify which Actor implementation to use: the __chaotic__ one or the __ordered__ one.

[AKKA](https://akka.io/) is a toolkit for Java and Scala. But given that events happen in non-deterministic (or chaotic) order in distributed systems, how best to impose order upon this chaos? The original blog post may be found [here](https://chariotsolutions.com/blog/post/order-chaos-maintaining-ordered-processing-messages-akka-actors/). The author arrives at a clever solution, although in my opinion the really clever part is the way the transactions are made to occur in random order.

## Options

The first, third and fourth options will all run the OrderedActor and give the correct result of $1500. The second option will give a semi-random result, generally something like $200 or $300 (both of which are incorrect).

1) `sbt run`
2) `sbt "run ChaosActor"`
3) `sbt "run OrderedActor"`
4) `sbt "run Chaos Monkey"`

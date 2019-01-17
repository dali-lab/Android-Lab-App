# Android DALI Lab App

A app for showing information the DALI members including upcoming events, devices available to be checked out, a list of who is in the lab, and more. It will also allow people to vote at DALI votable events, edit the state of the lights in the lab, check out equipment, and check into DALI events. Modeled after the [iOS Version](https://github.com/dali-lab/Lab-app).

## Inspiration
This Hack A Thing may have something to do with the project that I choose to work on, or it might not. 
I am very proficient in iOS development, so I knew that rounding out my experience and trying this new cleaner language would make it so that if I do a mobile project, I will know enough about both platforms that there will be nothing we cannot do!

## Learned
During this project, I leared how to do Android development in Kotlin, a language I had never exprienced before.
I have done some Android development in the past, but minimal work on networking, advanced UI techniques, etc.

## What didn't work
For the longest time, Google Sign In wasn't working. The problem with Google Sign In, it seams, is the incredibly sparse documentation on how to properly authenticate a project, especailly when that project has to be able to send those tokens to an API.
As I eventually found, the solution was to create credentials using the same Google credentials project as the API has. I also found the key generation process to be particularly problematic.

I also had problems with setup bluetooth beacon monitoring. In total, the major setback in this project, the thing that "Didn't work", was the Android documentation.

## Contributors
> John Kotz
The purpose of this project is to monitor burnination efforts on the Stack Exchange network. It tracks the progress by watching for close votes / reopen votes / delete votes / edits made to questions on the burninated tag.

It also involves a chat bot that posts notification in dedicated chat rooms.

##Chat bot

###`@burnaki start tag [tag] [rooms]...`

This command starts the burnination process of the given tag. All questions with that tag from SE API will be queried and stored in a database. The bot will send notification to the configured chat rooms about actions made to those questions.

###`@burnaki add room [room]`

This command adds the given chat room to the list of current configured chat rooms for the burnination effort in progress.

###`@burnaki remove room [room]`

This command removes the given chat room to the list of current configured chat rooms for the burnination effort in progress.

###`@burnaki get progress`

This command outputs the current progress of the burnination: number of opened questions / number of closed questions / number of questions with close-votes / number of questions with delete-votes.

###`@burnaki update progress [link to community-wiki]`

When a burnination effort is started and a community-wiki answer has been posted to track the progress on Meta, this command will 
edit the answer with the current progress.

###`@burnaki stop tag [tag]`

This command end the burnination process of the tag given tag. No new notifications will be sent.

##Notifications

During a burnination process, notifications will be sent to the configured chat rooms:

 - When a question has been closed. The goal is to ensure the question gets attraction for a potential inappropriate close.
 - When a reopen vote is cast on a closed question. This means that the question was potentially wrongly closed during the effort and needs to be re-reviewed.
 - When an edit is made to a question. This helps to track which questions were edited by whom and potentially warn wrong edits or users on an edit-spree.
 - When a delete vote is cast on a closed question.

##Web interface

Print nice progress graphs?

Generate one realistic tweet written by a fictional software developer.

Available topics:
{keywords}

Writing style for this user:
{writingStyle}

Emotional direction of this particular tweet:
{sentimentDirection}

Tweet requirements:
- write from a personal perspective as if sharing an opinion, experience, reaction, question or observation;
- naturally focus on one topic, optionally connecting it with one other topic from the list;
- express the requested emotional direction through wording, but never name or explain the sentiment;
- sound like a real social-media post, not documentation, marketing copy or a list of keywords;
- vary the opening, vocabulary and sentence structure;
- keep the text between 30 and 240 characters;
- use at most one hashtag and at most one emoji, and only when they fit naturally;
- do not mention these instructions, the generation process or that the author is fictional.

Use these metadata values exactly:
- createdAt: {currentTime}
- id: {tweetId}
- user.id: {userId}

Generation nonce: {requestId}
Use it only as a source of variation. Never include it in the tweet or JSON.

Return exactly one object matching this format:
{format}

SPARA
=====

A Recommender System for Exploratory Browsing


In the recent years we witnessed the rising of two interesting classes of user-centric applications: recommender systems and exploratory browsing tools.

On the one hand, recommendation systems show the user items that have a strong connection to his/her interests. On the other hand, by means of an exploratory browsing task the user is guided through the navigation of a knowledge space with the aim of finding new or serendipitous information.

With this mini-project we took the best from the two approaches by developing a system that leverages the vast amount of data and knowledge encoded in DBpedia by taking the user preferences into consideration.

These systems can also allow the users to explore these recommendations by setting the focus on one of the recommended items and then showing further items that are related both to the user interests and to the item on focus. This exploratory process can lead to the discovery of items belonging to different categories/knowledge-domains. For example, a user browsing through a set of films can discover that one of them is based on a book. At this point, the user can click on the book, changing its current domain of interest and start browsing books instead. We developed a multi-domain recommender system that exploits the linked open data from DBpedia. This allows the system to identify both relations between items, and the category they belong to. We compute the recommendations using Jaccard similarity and Cosine similarity on the features extracted from DBpedia. We used visualization attributes from the Semiology field which enables encode the most important information in the most perceptually accurate way. Our approach allows multi-domain exploration of recommendations with a set of domains of interest.

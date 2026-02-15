# libdanrfid
### RFID tags in libraries  
..contain mainly an item code, a library and country code. libdanrfid is meant to become a swiss army knife when dealing with tag content. Currently supported datamodels are 
+ the danish data model DDM
+ the bibliotheca coding called DM11

Basically the tags used in libraries are ISO 15693 Tags. These tags are passive in tha they have no battery, instead they are activated where in a reader's field. The tags could also be used as NFC tags, but NFC impies a flexible data scheme containing format specific metadata.
The DDM uses a fixed data format where all fields fit into 32 bytes, using block 0 to 7 with 4 bytes each.

### Whats the goal?

This library shall provide a reference implementation dealing with the different implementations of the Danish Data Model. While providing a quite strict definition, tags often cannot be interchanged between institutions. If there were a reference implementation, vendors could easily check compliance of their model against it.

read more in the Wiki

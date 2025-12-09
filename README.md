# diamond-moth-detection-mask-rcnn

This project aims to create an AI system to detect Diamondback Moths (DBM) from insect trap images containing many insects. This is so that when the number of DBMs exceeds a certain threshold, the system user will be alerted and the system user can take corrective actions.

<img width="530" height="540" alt="annotated image" src="https://github.com/user-attachments/assets/65c5c862-163b-4ed3-9129-3f3039239b18" />

**Some important features of DBMs:**
* white line on its back (distinguishing feature)
* opaque wings
* diamond-shaped body when wings are closed
* triangular-shaped body when wings are open
* long antennas
* black eyes
* general size is identical to other DBMs (sizes of insect compared to definite DBMs in image)
* greyish mush (indicates rotting)


**Main deliverables:**
* Dataset with 738 annotated images
* Statistics on the DBM dataset
* Fully functional Mask R-CNN model for the DBM dataset
* Fully functional split in 4 dataset Mask R-CNN model
* Discovery that this DBM detection system is considered to be Small Object Detection
* Exploring the common solutions to Small Object Detection problems
* Binary Image Classification model with 92.7% accuracy
* Research on Image Processing techniques and finding on the best technique for the model
* Functional android app

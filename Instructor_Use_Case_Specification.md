# Instructor Use Case Specification

Created By: SE2034-SWP391-G4  
Date Created: 12/07/2026  
Primary Actor: Instructor  

Source code references used: `InstructorCourseController`, `InstructorSectionController`, `InstructorLessonController`, `MaterialViewController`, `CourseService`, `CourseSectionService`, `LessonService`, `LessonMaterialService`, `DashboardInstructorService`.

---

## UC-22: Create Course

UC ID and Name:  
UC-22: Create Course

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor opens the course creation page and submits the basic course information.

Description:  
This use case allows an Instructor to initialize a new course by providing title, category, level, price, description, and optionally a thumbnail. The system saves the course with DRAFT status.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The Instructor has permission to access the instructor course management area.  
PRE-3. Active course categories are available in the system.

Postconditions:  
POST-1. A new course record is created and linked to the Instructor.  
POST-2. The course status is set to DRAFT.  
POST-3. If a thumbnail is uploaded, it is stored in Azure Blob Storage and linked to the course.

Normal Flow:  
1. The Instructor navigates to the course creation page.  
2. The system displays the course form with active categories and available course levels.  
3. The Instructor enters the title, category, level, price, description, and optional thumbnail.  
4. The Instructor submits the form.  
5. The system validates required fields and verifies that the Instructor does not already have a course with the same title.  
6. The system uploads the thumbnail to Azure Blob Storage if provided.  
7. The system creates the course, assigns it to the Instructor, sets status to DRAFT, and saves it.  
8. The system displays a success message and redirects the Instructor to either create another course or continue to curriculum management.

Alternative Flows:  
AF1: Save and Continue  
1. The Instructor selects the save-and-continue action.  
2. The system saves the course and redirects to the curriculum page for the new course.

Exceptions:  
E1: Missing or Invalid Required Fields  
If title, description, category, or price is missing or invalid, the system displays validation errors and does not create the course.  

E2: Duplicate Course Title  
If the Instructor already owns a course with the same title, the system displays an error and asks for a different title.  

E3: Thumbnail Upload Failure  
If thumbnail upload fails, the system stops the save operation and displays an error.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-01. Course title is required and must be at least 3 characters.  
BR-IC-02. Course title must be unique per Instructor.  
BR-IC-03. A newly created course must be saved as DRAFT.  
BR-IC-04. Course category must exist and be active.  
BR-IC-05. Course price is required and must be valid.

---

## UC-23: Edit Course

UC ID and Name:  
UC-23: Edit Course

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor selects an existing course and chooses to edit or delete it.

Description:  
This use case allows an Instructor to update or delete an existing course, including editable information such as title, category, level, price, description, thumbnail, and other supported fields.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected course exists.  
PRE-3. The selected course belongs to the current Instructor.

Postconditions:  
POST-1. The course information is updated, or the course is deleted.  
POST-2. If a new thumbnail is uploaded, the new file is stored and linked to the course.  
POST-3. The system displays a success or error message.

Normal Flow:  
1. The Instructor opens the course edit page.  
2. The system loads the current course information, sections, lessons, categories, and levels.  
3. The Instructor modifies course details.  
4. The Instructor submits the update form.  
5. The system validates the submitted data and checks duplicate title rules.  
6. The system uploads the new thumbnail if provided.  
7. The system saves the updated course information.  
8. The system redirects back to the edit page and displays a success message.

Alternative Flows:  
AF1: Delete Course  
1. The Instructor clicks the delete action for the course.  
2. The system deletes the course record.  
3. The system redirects the Instructor to the course list page.

Exceptions:  
E1: Access Denied  
If the course does not belong to the Instructor, the system denies access.  

E2: Course Not Found  
If the course does not exist, the system displays an error.  

E3: Invalid Course Data  
If submitted data is invalid, the system displays validation messages and does not save changes.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-01, BR-IC-02, BR-IC-04, BR-IC-05  
BR-IC-06. Only the owner Instructor can edit a course.

---

## UC-24: List Course

UC ID and Name:  
UC-24: List Course

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor opens the course management page.

Description:  
This use case allows an Instructor to view a paginated list of courses they created, grouped by status for browsing and management.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.

Postconditions:  
POST-1. Courses created by the Instructor are displayed by status.  
POST-2. Pagination information is available for each status tab.

Normal Flow:  
1. The Instructor navigates to the course management page.  
2. The system identifies the current Instructor.  
3. The system queries courses owned by the Instructor for statuses PUBLISHED, DRAFT, REJECTED, HIDDEN, and PENDING.  
4. The system sorts each list by last updated date in descending order.  
5. The system displays the course lists with pagination controls.

Alternative Flows:  
AF1: Navigate Between Status Pages  
1. The Instructor selects a page number in a course status group.  
2. The system reloads that page of courses for the selected status.

Exceptions:  
E1: No Courses Available  
If the Instructor has no courses for a status, the system displays an empty list for that status.

Priority:  
High - Must Have

Frequency of Use:  
High

Business Rules:  
BR-IC-07. An Instructor can only list courses created by them.  
BR-IC-08. Course management list supports DRAFT, PENDING, PUBLISHED, REJECTED, and HIDDEN statuses.

---

## UC-25: View Course

UC ID and Name:  
UC-25: View Course

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor selects a course to view details.

Description:  
This use case displays detailed information for a selected course, including course metadata, sections, lessons, materials, and current course content structure.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected course exists.

Postconditions:  
POST-1. The selected course detail page is displayed.  
POST-2. The Instructor can review sections, lessons, and material names.

Normal Flow:  
1. The Instructor selects a course from the course list.  
2. The system retrieves the course detail by course ID.  
3. The system loads course title, category, description, price, level, creation date, thumbnail, sections, lessons, and material names.  
4. The system calculates the total number of lessons.  
5. The system displays the course detail page.

Alternative Flows:  
None

Exceptions:  
E1: Course Not Found  
If the course does not exist, the system displays an error or redirects according to global error handling.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-09. Course details must include the curriculum structure when sections and lessons exist.

---

## UC-26: Create Section

UC ID and Name:  
UC-26: Create Section

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor submits a new section form within a course curriculum.

Description:  
This use case allows an Instructor to create a new section within an existing course to organize related lessons.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The target course exists.  
PRE-3. The Instructor is managing the target course curriculum.

Postconditions:  
POST-1. A new section is created under the selected course.  
POST-2. The section receives the next available position in the course.

Normal Flow:  
1. The Instructor opens the course curriculum or edit page.  
2. The Instructor enters the section title.  
3. The Instructor submits the section form.  
4. The system validates the section data.  
5. The system retrieves the target course.  
6. The system calculates the next section position.  
7. The system saves the section and displays a success message.  
8. The system redirects back to the curriculum or edit page based on the source page.

Alternative Flows:  
AF1: Create Section While Editing Course  
1. The Instructor creates the section from the course edit page.  
2. The system redirects back to the edit page after saving.

Exceptions:  
E1: Invalid Section Data  
If the section title or submitted data is invalid, the system displays an error and does not create the section.  

E2: Course Not Found  
If the target course does not exist, the system displays an error.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-10. A section must belong to one course.  
BR-IC-11. New sections are appended after the current maximum section position.

---

## UC-27: Edit Section

UC ID and Name:  
UC-27: Edit Section

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor submits changes for an existing section.

Description:  
This use case allows an Instructor to modify the title, position, or other editable information of an existing section. In the current implementation, the section title is updated and the updated timestamp is recorded.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The target section exists.  
PRE-3. The target section belongs to the selected course.

Postconditions:  
POST-1. The section information is updated.  
POST-2. The system redirects back to the curriculum or edit page.

Normal Flow:  
1. The Instructor opens the curriculum or edit page.  
2. The Instructor selects a section to edit.  
3. The Instructor updates the section title.  
4. The Instructor submits the edit form.  
5. The system validates the submitted data.  
6. The system updates the section title and updated timestamp.  
7. The system displays a success message.

Alternative Flows:  
AF1: Delete Section  
1. The Instructor clicks delete for a section.  
2. The system deletes the section.  
3. The system reorders later sections by decreasing their position by one.  
4. The system redirects to the source page.

Exceptions:  
E1: Invalid Section Data  
If submitted data is invalid, the system displays an error.  

E2: Section Not Found  
If the section does not exist, the system displays an error.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-12. Deleting a section must keep remaining section positions continuous.

---

## UC-28: List Section

UC ID and Name:  
UC-28: List Section

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor opens a course curriculum page.

Description:  
This use case displays all sections belonging to a selected course in their defined order, including lesson information where available.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected course exists.

Postconditions:  
POST-1. All sections for the selected course are displayed.  
POST-2. The total number of lessons is calculated for the curriculum page.

Normal Flow:  
1. The Instructor opens the curriculum page for a course.  
2. The system retrieves all sections for the course with their lessons.  
3. The system maps section ID, title, position, lessons, lesson materials, and quiz summary information.  
4. The system calculates the total number of lessons.  
5. The system renders the curriculum page.

Alternative Flows:  
None

Exceptions:  
E1: No Sections Available  
If the course has no sections, the system displays an empty curriculum area.

Priority:  
High - Must Have

Frequency of Use:  
High

Business Rules:  
BR-IC-13. Sections must be displayed by their course-defined position.

---

## UC-29: View Section

UC ID and Name:  
UC-29: View Section

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor expands or selects a section in the curriculum.

Description:  
This use case displays the details of a selected section, including its title, position, and the lessons contained within it.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected section exists.

Postconditions:  
POST-1. The selected section and its lesson list are visible to the Instructor.

Normal Flow:  
1. The Instructor opens the curriculum page.  
2. The Instructor selects or expands a section.  
3. The system displays the section title, position, and lesson list.  
4. The system displays related lesson metadata such as duration, preview status, quizzes, and materials where available.

Alternative Flows:  
None

Exceptions:  
E1: Section Has No Lessons  
If the section has no lessons, the system displays the section without lesson rows.

Priority:  
Medium - Should Have

Frequency of Use:  
High

Business Rules:  
BR-IC-14. A section detail view must show lessons belonging only to that section.

---

## UC-30: Create Lesson

UC ID and Name:  
UC-30: Create Lesson

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor submits a new lesson form inside a section.

Description:  
This use case allows an Instructor to create a new lesson within a selected section by providing lesson information such as title, video, duration, preview setting, and optional material files.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The target section exists.  
PRE-3. The Instructor is managing the related course.

Postconditions:  
POST-1. A new lesson is created under the selected section.  
POST-2. The video file is stored in Azure Blob Storage.  
POST-3. Optional material files are uploaded and linked to the lesson.  
POST-4. The lesson moderation status is set to PENDING.

Normal Flow:  
1. The Instructor opens the curriculum or edit page.  
2. The Instructor selects the target section.  
3. The Instructor enters lesson title, uploads a video, sets duration and free preview option, and optionally uploads material files.  
4. The Instructor submits the lesson form.  
5. The system validates that the section exists and lesson data is valid.  
6. The system checks that the lesson title is not duplicated within the section.  
7. The system uploads the video to Azure Blob Storage.  
8. The system calculates the next lesson position in the section.  
9. The system saves the lesson as unpublished with moderation status PENDING.  
10. The system uploads and saves optional materials.  
11. The system redirects to the curriculum or edit page and displays a success message.

Alternative Flows:  
AF1: Create Lesson Without Materials  
1. The Instructor submits a lesson with a video but without material files.  
2. The system creates the lesson and skips material upload.

Exceptions:  
E1: Missing Video  
If no video file is provided, the system displays an error and does not create the lesson.  

E2: Duplicate Lesson Title  
If the lesson title already exists in the section, the system displays an error.  

E3: Invalid Section  
If the target section does not exist, the system displays an error.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-15. Lesson title is required and must not exceed 255 characters.  
BR-IC-16. Lesson title must be unique within the same section.  
BR-IC-17. A lesson must have a video when created.  
BR-IC-18. New lessons are appended after the current maximum lesson position.  
BR-IC-19. New or updated lessons must be marked for moderation as PENDING.

---

## UC-31: View Lesson

UC ID and Name:  
UC-31: View Lesson

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor selects a lesson detail page.

Description:  
This use case displays detailed information and learning content of a selected lesson, including course, section, lesson metadata, materials, and quizzes.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected lesson exists.  
PRE-3. The Instructor has access to the lesson.

Postconditions:  
POST-1. The lesson detail page is displayed.  
POST-2. The Instructor can view lesson materials and quiz information.

Normal Flow:  
1. The Instructor opens a lesson detail page.  
2. The system identifies the current Instructor.  
3. The system retrieves the lesson, parent section, parent course, lesson materials, and quizzes.  
4. The system displays lesson information and paginated quiz list.  
5. The Instructor reviews lesson content and related resources.

Alternative Flows:  
AF1: View Video  
1. The Instructor requests to view the lesson video.  
2. The system checks access rights.  
3. The system generates a video access URL from Azure Blob Storage and redirects the Instructor.

Exceptions:  
E1: Lesson Not Found  
If the lesson does not exist, the system displays an error.  

E2: Access Denied  
If the Instructor does not own the related course, the system denies access.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-20. Instructors can access lessons only for courses they own, while Admin and Manager can also access them.

---

## UC-32: List Lesson

UC ID and Name:  
UC-32: List Lesson

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor views a section in the course curriculum.

Description:  
This use case displays all lessons contained within a selected section in their defined order.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected section exists.

Postconditions:  
POST-1. Lessons in the section are displayed to the Instructor.

Normal Flow:  
1. The Instructor opens a course curriculum page.  
2. The system retrieves sections and lessons for the selected course.  
3. For each section, the system loads lesson ID, title, position, duration, free preview flag, quiz summary, and material summary.  
4. The system displays lessons under their parent section.

Alternative Flows:  
None

Exceptions:  
E1: No Lessons Available  
If the section has no lessons, the system displays an empty lesson list for that section.

Priority:  
High - Must Have

Frequency of Use:  
High

Business Rules:  
BR-IC-21. Lessons must be displayed under their own section and ordered by position.

---

## UC-33: Edit Lesson

UC ID and Name:  
UC-33: Edit Lesson

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor submits changes to an existing lesson.

Description:  
This use case allows an Instructor to update the title, video, position-related data, attachments, preview setting, duration, or other editable information of an existing lesson.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected lesson exists.  
PRE-3. The Instructor is managing the related course.

Postconditions:  
POST-1. The lesson is updated.  
POST-2. If a new video is provided, the old video is deleted from Azure Blob Storage when possible and the new video is saved.  
POST-3. New uploaded materials are linked to the lesson.  
POST-4. The lesson moderation status is set to PENDING.

Normal Flow:  
1. The Instructor opens the curriculum or edit page.  
2. The Instructor selects a lesson to edit.  
3. The Instructor updates the lesson title, duration, free preview setting, video file, and optional material files.  
4. The Instructor submits the update form.  
5. The system validates the lesson ID and title.  
6. The system checks that the updated title is not duplicated in the same section.  
7. If a new video is uploaded, the system deletes the old video from Azure Blob Storage when possible and uploads the new video.  
8. The system updates lesson fields and sets moderation status to PENDING.  
9. The system saves new material files if provided.  
10. The system redirects to the curriculum or edit page and displays a success message.

Alternative Flows:  
AF1: Update Lesson Without Replacing Video  
1. The Instructor updates text fields only.  
2. The system keeps the existing video URL and saves the other changes.

AF2: Delete Lesson  
1. The Instructor clicks delete for a lesson.  
2. The system deletes the lesson video from Azure Blob Storage when possible.  
3. The system deletes related materials when possible.  
4. The system deletes the lesson record and redirects to the edit page.

Exceptions:  
E1: Invalid Lesson ID  
If the lesson ID is missing or invalid, the system displays an error.  

E2: Duplicate Lesson Title  
If the updated title already exists in the same section, the system displays an error.  

E3: File Delete Warning  
If the old video or material file cannot be deleted from storage, the system logs a warning and continues the database operation.

Priority:  
High - Must Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-15, BR-IC-16, BR-IC-19  
BR-IC-22. Replacing a lesson video should remove the previous stored video when possible.  
BR-IC-23. Deleting a lesson should also remove its stored video and associated materials when possible.

---

## UC-34: Create Material

UC ID and Name:  
UC-34: Create Material

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor uploads supplemental learning resources while creating or editing a lesson.

Description:  
This use case allows an Instructor to upload supplemental learning resources such as PDFs, slides, documents, spreadsheets, or presentations linked to lessons and stored securely on Azure Blob Storage.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The target lesson exists.  
PRE-3. The Instructor has access to manage the target lesson.

Postconditions:  
POST-1. Uploaded files are stored in Azure Blob Storage.  
POST-2. Material records are created and linked to the lesson.  
POST-3. Material metadata such as file name, file type, file size, file URL, and Instructor are stored.

Normal Flow:  
1. The Instructor selects one or more material files for a lesson.  
2. The Instructor submits the lesson create or edit form.  
3. The system retrieves the target lesson.  
4. For each non-empty file, the system reads the original file name, extension, and size.  
5. The system uploads the file to Azure Blob Storage in the materials container.  
6. The system creates a lesson material record linked to the lesson and Instructor.  
7. The system saves the material record.

Alternative Flows:  
AF1: No Material Selected  
1. The Instructor submits the lesson form without material files.  
2. The system skips material creation.

Exceptions:  
E1: Lesson Not Found  
If the lesson does not exist, the system displays an error.  

E2: Empty File  
If a selected file is empty, the system skips that file.

Priority:  
Medium - Should Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-24. A material must be linked to one lesson.  
BR-IC-25. Material metadata must include file name, file type, file size, storage URL, and Instructor.

---

## UC-35: List Materials

UC ID and Name:  
UC-35: List Materials

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor opens a lesson detail or curriculum view containing materials.

Description:  
This use case allows the Instructor to view all uploaded material files along with their names, formats, sizes, and usage status where available.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected lesson exists.

Postconditions:  
POST-1. Materials linked to the selected lesson are displayed.  
POST-2. The Instructor can identify available files for preview, download, or deletion.

Normal Flow:  
1. The Instructor opens the curriculum or lesson detail page.  
2. The system retrieves materials associated with the lesson.  
3. The system displays material names and metadata available in the lesson material records.  
4. The Instructor reviews the list of uploaded files.

Alternative Flows:  
None

Exceptions:  
E1: No Materials Available  
If the lesson has no materials, the system displays an empty material list.

Priority:  
Medium - Should Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-26. Only materials attached to the selected lesson are listed in that lesson context.

---

## UC-36: View Material Detail

UC ID and Name:  
UC-36: View Material Detail

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage, Office Online Viewer

Trigger:  
The Instructor opens or previews a material file.

Description:  
This use case allows the Instructor to preview material content and view its technical specifications. PDF files are opened directly from storage, while Office document formats are opened through an Office viewer URL.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected material exists.  
PRE-3. The Instructor has access to the related course.

Postconditions:  
POST-1. The material preview URL is returned or the browser is redirected to a preview/download URL.  
POST-2. Unauthorized users are blocked.

Normal Flow:  
1. The Instructor selects a material to view.  
2. The system retrieves the current user session.  
3. The system loads the material record by ID.  
4. The system checks whether the user has access to the material.  
5. The system generates or retrieves the public material URL from Azure Blob Storage.  
6. If the file is PDF, the system returns or redirects to the file URL.  
7. If the file is DOC, DOCX, XLS, XLSX, PPT, or PPTX, the system returns or redirects to the Office viewer URL.  
8. For other file types, the system redirects to download.

Alternative Flows:  
AF1: Download Material  
1. The Instructor chooses to download the material.  
2. The system checks access rights.  
3. The system streams the file as a downloadable response.

Exceptions:  
E1: Not Logged In  
If no user session exists, the system redirects to login or returns unauthorized.  

E2: Material Not Found  
If the material does not exist, the system returns null, not found, or redirects based on the endpoint.  

E3: Access Denied  
If the user does not have access, the system blocks the request or redirects to the course page.

Priority:  
Medium - Should Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-27. Instructors may access materials only for courses they own, while Admin and Manager can also access them.  
BR-IC-28. Learners may access material only after enrollment in the related course.

---

## UC-37: Edit Material

UC ID and Name:  
UC-37: Edit Material

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
Azure Blob Storage

Trigger:  
The Instructor replaces, updates, or deletes an existing material file.

Description:  
This use case allows the Instructor to replace an existing material file, update its name, or delete it from the lesson material library. In the current implementation, adding new files during lesson edit and deleting existing material records are supported.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.  
PRE-2. The selected material or lesson exists.  
PRE-3. The Instructor has permission to manage the related lesson.

Postconditions:  
POST-1. New material files are added, or the selected material is deleted.  
POST-2. Deleted material files are removed from Azure Blob Storage when possible.  
POST-3. Material records are updated accordingly.

Normal Flow:  
1. The Instructor opens the lesson edit form.  
2. The Instructor uploads additional material files.  
3. The system saves each new material and links it to the lesson.  
4. The system displays the updated lesson material list.

Alternative Flows:  
AF1: Delete Material  
1. The Instructor clicks delete for a material.  
2. The system validates the material ID.  
3. The system loads the material record.  
4. The system deletes the stored file from Azure Blob Storage when possible.  
5. The system deletes the material record from the database.  
6. The system redirects back to the curriculum page and displays a success message.

AF2: Replace Material  
1. The Instructor deletes the old material.  
2. The Instructor uploads the replacement material in the lesson edit form.  
3. The system saves the replacement as a new material record.

Exceptions:  
E1: Invalid Material ID  
If the material ID is missing or invalid, the system displays an error.  

E2: Material Not Found  
If the material does not exist, the system displays an error.  

E3: Storage Delete Warning  
If the file cannot be deleted from Azure Blob Storage, the system logs a warning and deletes the database record.

Priority:  
Medium - Should Have

Frequency of Use:  
Medium

Business Rules:  
BR-IC-29. Deleting a material should also delete its stored file when possible.  
BR-IC-30. Replacement can be performed by deleting the old material and uploading a new one.

---

## UC-42: View Instructor Dashboard

UC ID and Name:  
UC-42: View Instructor Dashboard

Created By:  
SE2034-SWP391-G4

Date Created:  
12/07/2026

Primary Actor:  
Instructor

Secondary Actors:  
None

Trigger:  
The Instructor opens the dashboard page.

Description:  
This use case displays the instructor dashboard, providing an overview of courses, enrollments, revenue, learner statistics, top selling courses, course performance, recent orders, and revenue trends.

Preconditions:  
PRE-1. The Instructor has successfully logged into the system.

Postconditions:  
POST-1. Dashboard statistics are displayed for the selected period.  
POST-2. The Instructor can review revenue, orders, students, course counts, top selling courses, performance, recent orders, and chart data.

Normal Flow:  
1. The Instructor navigates to the dashboard page.  
2. The system identifies the current Instructor.  
3. The system loads Instructor profile information.  
4. The system resolves the selected period filter: MONTH, YEAR, CUSTOM, or default.  
5. The system calculates the current and previous date ranges.  
6. The system retrieves total revenue, order count, distinct student count, published courses, new courses, top selling courses, course performance, recent orders, and revenue trend data.  
7. The system calculates percentage changes compared with the previous period.  
8. The system renders the dashboard page with statistics and chart data.

Alternative Flows:  
AF1: Custom Month Filter  
1. The Instructor selects a custom year and month.  
2. The system calculates the first and last day of that month.  
3. The system displays dashboard statistics for the selected month.

AF2: No Revenue Trend Data  
1. The selected period has no revenue data.  
2. The system displays a default chart line and zero-value statistics where applicable.

Exceptions:  
E1: Invalid Custom Period  
If the custom year or month is missing or invalid, the system cannot resolve the custom date range and returns an error according to global error handling.

Priority:  
High - Must Have

Frequency of Use:  
High

Business Rules:  
BR-IC-31. Dashboard data must be calculated only for the current Instructor.  
BR-IC-32. Supported dashboard period filters include MONTH, YEAR, CUSTOM, and default all-time range.  
BR-IC-33. Percentage delta compares the current selected period with the immediately previous period of the same length.

INSERT INTO KSPL_LRNG_PLAN_TYPE (TYPE_KEY, OBJ_ID, VER_NBR, TYPE_DESC, NAME) VALUES ('kuali.academicplan.type.plan', 'plan-type-1', 0, 'Learning plan description', 'Learning Plan');
INSERT INTO KSPL_LRNG_PLAN_TYPE (TYPE_KEY, OBJ_ID, VER_NBR, TYPE_DESC, NAME) VALUES ('kuali.academicplan.type.plan.template', 'plan-type-2', 0, 'Learning plan template description', 'Learning Plan Template');

INSERT INTO KSPL_MYPLAN_RICH_TEXT (ID, FORMATTED, PLAIN, VER_NBR) VALUES ('lp1-Desc', '<p>Student 1 Learning Plan 1</p>',  'Student 1 Learning Plan 1', 0);
INSERT INTO KSPL_MYPLAN_RICH_TEXT (ID, FORMATTED, PLAIN, VER_NBR) VALUES ('lp2-Desc', '<p>Student 1 Learning Plan 2</p>',  'Student 1 Learning Plan 2', 0);
INSERT INTO KSPL_MYPLAN_RICH_TEXT (ID, FORMATTED, PLAIN, VER_NBR) VALUES ('lp3-Desc', '<p>Student 2 Learning Plan 1</p>',  'Student 2 Learning Plan 1', 0);
INSERT INTO KSPL_MYPLAN_RICH_TEXT (ID, FORMATTED, PLAIN, VER_NBR) VALUES ('lp4-Desc', '<p>Student 2 Learning Plan 2</p>',  'Student 2 Learning Plan 2', 0);

INSERT INTO KSPL_LRNG_PLAN (ID, OBJ_ID, VER_NBR, STUDENT_ID, RT_DESCR_ID, TYPE_ID) values ('lp1', 'obj-lp1', 0, 'student1', 'lp1-Desc', 'kuali.academicplan.type.plan');
INSERT INTO KSPL_LRNG_PLAN (ID, OBJ_ID, VER_NBR, STUDENT_ID, RT_DESCR_ID, TYPE_ID) values ('lp2', 'obj-lp2', 0, 'student1', 'lp2-Desc', 'kuali.academicplan.type.plan');
INSERT INTO KSPL_LRNG_PLAN (ID, OBJ_ID, VER_NBR, STUDENT_ID, RT_DESCR_ID, TYPE_ID) values ('lp3', 'obj-lp3', 0, 'student2', 'lp3-Desc', 'kuali.academicplan.type.plan');
INSERT INTO KSPL_LRNG_PLAN (ID, OBJ_ID, VER_NBR, STUDENT_ID, RT_DESCR_ID, TYPE_ID) values ('lp4', 'obj-lp4', 0, 'student2', 'lp4-Desc', 'kuali.academicplan.type.plan');

INSERT INTO KSPL_LRNG_PLAN_ITEM_TYPE (TYPE_KEY, OBJ_ID, VER_NBR, TYPE_DESC, NAME) VALUES ('kuali.academicplan.item.planned', 'obj-plan-item-planned', 0, 'Planned item description', 'Planned Item');
INSERT INTO KSPL_LRNG_PLAN_ITEM_TYPE (TYPE_KEY, OBJ_ID, VER_NBR, TYPE_DESC, NAME) VALUES ('kuali.academicplan.item.wishlist', 'obj-plan-item-wishlist', 0, 'Wishlist item description', 'Wishlist Item');
INSERT INTO KSPL_LRNG_PLAN_ITEM_TYPE (TYPE_KEY, OBJ_ID, VER_NBR, TYPE_DESC, NAME) VALUES ('kuali.academicplan.item.whatif', 'obj-plan-item-whatif', 0, 'What-if description', 'What-if Item');

INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp1-i1', 0, '006476b5-18d8-4830-bbb6-2bb9e79600fb', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.wishlist', 'lp1');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp1-i2', 0, '008d8bea-d63d-43fc-8a77-e52634202f6e', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.wishlist', 'lp1');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp1-i3', 0, '00ac5436-7014-4d54-95d6-98a6aeaa70c7', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.wishlist', 'lp1');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp1-i4', 0, '00ee833d-f4d6-4aca-b40f-8f80854f8cb3', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp1');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp1-i5', 0, '0005df5d-82e9-4663-8440-aee5ad8046d4', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp1');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp1-i6', 0, '00edd03d-d7c0-4151-b7f0-8977951c75bd', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp1');

INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp2-i1', 0, '00b3683b-fd14-4771-af6c-3c69a43f4592', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp2');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp2-i2', 0, '00a4c70c-051c-42f9-b45e-6488986a374d', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp2');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp2-i3', 0, '015f969b-9c18-4938-bcad-39c8a51f230d', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp2');

INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp3-i1', 0, '012cc945-7fa1-4df0-9124-8913f250eaf6', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.wishlist', 'lp3');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp3-i2', 0, '008739f0-8e62-4f98-88d1-30e6ac779b11', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.wishlist', 'lp3');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp3-i3', 0, '0131b128-d2dd-47f5-bdc0-3950b81f00e9', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.wishlist', 'lp3');

INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp4-i1', 0, '0099ce3a-0fe8-4586-96b6-c42f6db7c11e', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.whatif', 'lp4');
INSERT INTO KSPL_LRNG_PLAN_ITEM (ID, VER_NBR, REF_OBJ_ID, REF_OBJ_TYPE_KEY, TYPE_ID, PLAN_ID) VALUES ('lp4-i2', 0, '02711400-c66d-4ecb-aca5-565118f167cf', 'kuali.lu.type.CreditCourse', 'kuali.academicplan.item.planned', 'lp4');

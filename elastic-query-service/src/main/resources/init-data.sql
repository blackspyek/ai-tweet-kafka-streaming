CREATE EXTENSION IF NOT EXISTS "uuid-ossp";


INSERT INTO public.users(
    id, username, firstname, lastname
)
VALUES (
           'b27dceec-c87b-4972-be05-aba96500b917',
           'app_user',
           'Standard',
           'User'
       );

INSERT INTO public.users(
    id, username, firstname, lastname
)
VALUES (
           'a6c369af-a720-4537-923e-0556352938dd',
           'app_admin',
           'Admin',
           'User'
       );

INSERT INTO public.users(
    id, username, firstname, lastname
)
VALUES (
           '2103b4b1-9e20-47a6-9340-5f47aa674b28',
           'app_super_user',
           'Super',
           'User'
       );


INSERT INTO documents(id, document_id)
VALUES (
           'c1df7d01-4bd7-40b6-86da-7e2ffabf37f7',
           1
       );

INSERT INTO documents(id, document_id)
VALUES (
           'f2b2d644-3a08-4acb-ae07-20569f6f2a01',
           2
       );

INSERT INTO documents(id, document_id)
VALUES (
           '90573d2b-9a5d-409e-bbb6-b94189709a19',
           3
       );


INSERT INTO user_permissions(
    user_permission_id,
    user_id,
    document_id,
    permission_type
)
VALUES (
           uuid_generate_v4(),
           'b27dceec-c87b-4972-be05-aba96500b917',
           'c1df7d01-4bd7-40b6-86da-7e2ffabf37f7',
           'READ'
       );

INSERT INTO user_permissions(
    user_permission_id,
    user_id,
    document_id,
    permission_type
)
VALUES (
           uuid_generate_v4(),
           'a6c369af-a720-4537-923e-0556352938dd',
           'c1df7d01-4bd7-40b6-86da-7e2ffabf37f7',
           'READ'
       );

INSERT INTO user_permissions(
    user_permission_id,
    user_id,
    document_id,
    permission_type
)
VALUES (
           uuid_generate_v4(),
           'a6c369af-a720-4537-923e-0556352938dd',
           'f2b2d644-3a08-4acb-ae07-20569f6f2a01',
           'READ'
       );

INSERT INTO user_permissions(
    user_permission_id,
    user_id,
    document_id,
    permission_type
)
VALUES (
           uuid_generate_v4(),
           'a6c369af-a720-4537-923e-0556352938dd',
           '90573d2b-9a5d-409e-bbb6-b94189709a19',
           'READ'
       );

INSERT INTO user_permissions(
    user_permission_id,
    user_id,
    document_id,
    permission_type
)
VALUES (
           uuid_generate_v4(),
           '2103b4b1-9e20-47a6-9340-5f47aa674b28',
           'c1df7d01-4bd7-40b6-86da-7e2ffabf37f7',
           'READ'
       );
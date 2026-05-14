-- ============================================================
-- HHS Case Management Demo Seed Data
-- ============================================================

-- Staff users (passwords stored as BCrypt hashes; plaintext for demo reference below)
-- admin@hhs.gov    / Admin1234!
-- caseworker@hhs.gov / Case1234!

-- Passwords hashed with SHA-256 to match AuthService.hashPassword()
-- Admin1234!  => 5ce41ada64f1e8ffb0acfaafa622b141438f3a5777785e7f0b830fb73e40d3d6
-- Case1234!   => 932f33c32393f9fb76ff162e00a2ccae95f9b0ede44471a5a6c90258b18d080d
INSERT INTO app_user (id, email, password_hash, full_name, role, active, created_date)
VALUES
  (1, 'admin@hhs.gov',       '5ce41ada64f1e8ffb0acfaafa622b141438f3a5777785e7f0b830fb73e40d3d6', 'Patricia Morales',   'ADMIN',      true, CURRENT_TIMESTAMP),
  (2, 'caseworker@hhs.gov',  '932f33c32393f9fb76ff162e00a2ccae95f9b0ede44471a5a6c90258b18d080d', 'James Okonkwo',      'CASEWORKER', true, CURRENT_TIMESTAMP),
  (3, 'caseworker2@hhs.gov', '932f33c32393f9fb76ff162e00a2ccae95f9b0ede44471a5a6c90258b18d080d', 'Maria Tran',         'CASEWORKER', true, CURRENT_TIMESTAMP);

-- Sample clients
INSERT INTO client (id, first_name, last_name, date_of_birth, ssn_last_four, phone, email, address_line1, city, state_code, zip, created_date)
VALUES
  (1, 'Robert',   'Hutchins',  '1978-03-14', '4821', '801-555-0142', 'rhutchins@email.com',  '247 Maple Street',    'Salt Lake City', 'UT', '84101', CURRENT_TIMESTAMP),
  (2, 'Sandra',   'Yee',       '1965-09-02', '3309', '801-555-0287', 'syee@email.com',        '89 Canyon Road',      'Provo',          'UT', '84601', CURRENT_TIMESTAMP),
  (3, 'Marcus',   'Delgado',   '1990-11-27', '7714', '385-555-0193', 'mdelgado@email.com',    '1402 West Temple St', 'Salt Lake City', 'UT', '84116', CURRENT_TIMESTAMP),
  (4, 'Theresa',  'Kaminski',  '1952-06-19', '2256', '435-555-0311', 'tkaminski@email.com',   '33 Pioneer Blvd',     'Ogden',          'UT', '84401', CURRENT_TIMESTAMP);

-- Sample service applications
INSERT INTO service_application (id, client_id, assigned_to_id, service_type, status, priority, submitted_date, last_updated, notes)
VALUES
  (1, 1, 2, 'MEDICAID',          'IN_REVIEW',  'NORMAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Initial application submitted online.'),
  (2, 2, 2, 'FOOD_ASSISTANCE',   'PENDING',    'HIGH',   CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Client reported loss of employment.'),
  (3, 3, 3, 'HOUSING_SUPPORT',   'APPROVED',   'NORMAL', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Approved for 90-day transitional housing assistance.'),
  (4, 4, 2, 'DISABILITY_SVCS',   'PENDING',    'URGENT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Referred by primary care physician.');

-- Sample case notes
INSERT INTO case_note (id, application_id, author_id, note_text, created_date)
VALUES
  (1, 1, 2, 'Contacted client to request income verification documents. Client will fax by end of week.', CURRENT_TIMESTAMP),
  (2, 2, 2, 'Verified employment separation. Client eligible for expedited processing under hardship guidelines.', CURRENT_TIMESTAMP),
  (3, 3, 3, 'Placement confirmed at Sunrise Transitional Housing. Move-in scheduled for next Monday.', CURRENT_TIMESTAMP),
  (4, 4, 2, 'Requested medical records from Dr. Nguyen office. Follow-up call scheduled.', CURRENT_TIMESTAMP);

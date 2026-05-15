ALTER TABLE wf_definitions ADD COLUMN complexity VARCHAR(255) DEFAULT 'Low';
ALTER TABLE wf_definitions ADD COLUMN rating INT DEFAULT 3;
ALTER TABLE wf_definitions ADD COLUMN owner_name VARCHAR(255) DEFAULT 'Ioni owcher';
ALTER TABLE wf_definitions ADD COLUMN owner_avatar_url VARCHAR(255) DEFAULT 'https://ui-avatars.com/api/?name=Ioni+Owcher&background=random';
ALTER TABLE wf_definitions ADD COLUMN last_updated VARCHAR(255) DEFAULT '09/13/2015';
ALTER TABLE wf_definitions ADD COLUMN version_label VARCHAR(255) DEFAULT 'v 1.0';
ALTER TABLE wf_definitions ADD COLUMN enabled BOOLEAN DEFAULT TRUE;

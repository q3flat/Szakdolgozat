CREATE TRIGGER pokerdb.TR_BEFORE_CREATE_TABLE BEFORE INSERT ON pokerdb.poker_tables
	FOR EACH ROW
	BEGIN
		IF (CHAR_LENGTH(NEW.name) > 30) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_POKER_TABLES_NAME_LENGHT';
		end IF;
		IF (!(5 <= NEW.max_time && NEW.max_time <= 40)) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_POKER_TABLES_MAX_TIME';
		end IF;
	
		IF (!(2 <= NEW.max_players && NEW.max_players <= 5)) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_POKER_TABLES_MAX_PLAYERS';
		end IF;
	END;
/

CREATE TRIGGER pokerdb.TR_BEFORE_UPDATE_TABLE BEFORE UPDATE ON pokerdb.poker_tables
	FOR EACH ROW
	BEGIN
		IF (CHAR_LENGTH(NEW.name) > 30) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_POKER_TABLES_NAME_LENGHT';
		end IF;
		IF (!(5 <= NEW.max_time && NEW.max_time <= 40)) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_POKER_TABLES_MAX_TIME';
		end IF;
	
		IF (!(2 <= NEW.max_players && NEW.max_players <= 5)) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_POKER_TABLES_MAX_PLAYERS';
		end IF;
	END;
/

CREATE TRIGGER pokerdb.TR_BEFORE_CREATE_USER BEFORE INSERT ON pokerdb.users
	FOR EACH ROW
	BEGIN
		SET @c = CHAR_LENGTH(NEW.username);
		IF (!(3 <= @c && @c <= 20)) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_USERS_USERNAME_LENGHT';
		END IF;
		
		IF (CHAR_LENGTH(NEW.password) > 64) THEN
			SIGNAL SQLSTATE '45000'
			SET MESSAGE_TEXT = 'CONSTRAINT_USERS_PASSWORD_LENGHT';
		END IF;
	
		SET NEW.reg_date = UNIX_TIMESTAMP();
	END;
/
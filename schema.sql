--Just for getting syntax highlighting -> need to be copied into ClanManagerConfig.java

CREATE TABLE IF NOT EXISTS "clan" (
    "id" SERIAL PRIMARY KEY,
    "verificationCode" TEXT NOT NULL UNIQUE,
    "name" TEXT NOT NULL,
    "tag" TEXT NOT NULL,
    "ownerId" INT NOT NULL,
    "ownerUserId" BIGINT NOT NULL,
    "discordGuildId" BIGINT NOT NULL,
    "leaderShipRoleId" BIGINT NOT NULL,
    "memberRoleId" BIGINT NOT NULL,
    "discordChannelId" BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS "clanMember" (
    "id" SERIAL PRIMARY KEY,
    "verificationTime" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "nickname" TEXT NOT NULL,
    "permission" TEXT NOT NULL DEFAULT "MEMBER",
    "locale" TEXT NOT NULL,
    "clanId" INT NOT NULL,
    "discordUserId" BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS "reverificationFeature" (
    "clanId" INT PRIMARY KEY,
    "numberOfDays" SMALLINT NOT NULL DEFAULT 90
);

CREATE TABLE IF NOT EXISTS "blockedUsers" (
    "clanId" INT UNIQUE,
    "discordUserId" BIGINT UNIQUE,
    PRIMARY KEY ("clanId", "discordUserId")
);
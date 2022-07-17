CREATE TABLE IF NOT EXISTS "clan" (
    "id" SERIAL PRIMARY KEY,
    "verificationCode" TEXT NOT NULL UNIQUE,
    "name" TEXT NOT NULL,
    "tag" TEXT NOT NULL,
    "discordGuildId" BIGINT NOT NULL,
    "ownerId" BIGINT NOT NULL,
    "leaderShipRoleId" INT NOT NULL,
    "memberRoleId" BIGNT NOT NULL,
    "discordChannelId" BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS "clanMember" (
    "id" SERIAL PRIMARY KEY,
    "verificationTime" TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "nickname" TEXT NOT NULL,
    "leaderShipStatus" BOOLEAN NOT NULL DEFAULT FALSE,
    "coOwnerStatus" BOOLEAN NOT NULL DEFAULT FALSE,
    "locale" TEXT NOT NULL,
    "clanId" INT NOT NULL,
    "discordUserId" BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS "reverificationFeature" (
    "clanId" INT PRIMARY KEY,
    "numberOfDays" SMALLINT NOT NULL DEFAULT 90
);
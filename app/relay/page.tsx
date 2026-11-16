import type { Metadata } from "next";

import { RelayApp } from "./relay-app";

export const metadata: Metadata = {
  title: "Relay workspace",
  description: "A representative access and readiness workspace for Android automation.",
};

const RelayPage = () => <RelayApp />;

export default RelayPage;


import { ScenarioPlanner } from "@/components/ScenarioPlanner";

export default async function ScenarioPage({ params }: { params: Promise<{ goalId: string }> }) {
  const { goalId } = await params;
  return <ScenarioPlanner goalId={goalId} />;
}

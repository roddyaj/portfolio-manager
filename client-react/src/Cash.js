function Cash(props) {
	const { portfolio } = props;

	const positions = portfolio.positions.filter(p => !p.option);
	const sellTotal = positions.filter(p => p.sharesToBuy < 0).map(p => p.sharesToBuy * p.price).reduce((a, b) => a + b, 0);
	const buyTotal = positions.filter(p => p.sharesToBuy > 0).map(p => p.sharesToBuy * p.price).reduce((a, b) => a + b, 0);

	return (
		<div className="pm-block">
			<div className="pm-heading">
				<div className="pm-title">Cash</div>
			</div>
			<table>
				<tbody>
					<tr><td className="l">Cash</td><td>{portfolio.cash.toFixed(2)}</td></tr>
					<tr><td className="l">Cash on hold</td><td>- {portfolio.cashOnHold.toFixed(2)}</td></tr>
					<tr><td className="l">Open buys</td><td>- {portfolio.openBuyAmount.toFixed(2)}</td></tr>
					<tr style={{ borderTop: "1px solid grey" }}><td className="l">Available cash</td><td>{portfolio.cashAvailable.toFixed(2)}</td></tr>
					<tr><td className="l"><span style={{ display: "inline-block" }} /></td><td></td></tr>
					<tr><td className="l">To sell</td><td>{sellTotal.toFixed(2)}</td></tr>
					<tr><td className="l">To buy</td><td>{buyTotal.toFixed(2)}</td></tr>
				</tbody>
			</table>
		</div>
	);
}

export default Cash;
